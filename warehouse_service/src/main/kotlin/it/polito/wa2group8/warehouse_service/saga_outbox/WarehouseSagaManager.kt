package it.polito.wa2group8.warehouse_service.saga_outbox

import io.debezium.config.Configuration
import io.debezium.data.Envelope
import io.debezium.embedded.Connect
import io.debezium.engine.DebeziumEngine
import io.debezium.engine.RecordChangeEvent
import io.debezium.engine.format.ChangeEventFormat
import it.polito.wa2group8.warehouse_service.domain.*
import it.polito.wa2group8.warehouse_service.dto.QuantityAlarmEvent
import it.polito.wa2group8.warehouse_service.dto.TO_CATALOG_TOPIC
import it.polito.wa2group8.warehouse_service.repositories.ProductRepository
import it.polito.wa2group8.warehouse_service.repositories.ProductWarehouseRepository
import it.polito.wa2group8.warehouse_service.repositories.WarehouseOutboxRepository
import it.polito.wa2group8.warehouse_service.saga_outbox.events.*
import org.apache.kafka.connect.data.Struct
import org.apache.kafka.connect.source.SourceRecord
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import java.util.concurrent.Executor
import java.util.concurrent.Executors
import javax.annotation.PostConstruct
import javax.annotation.PreDestroy

/**
 * This class handle orders (warehouse side) according patterns Saga and Outbox
 */
@Transactional
@Component
class WarehouseSagaManager(
    private val productRepository: ProductRepository,
    private val pwRepository: ProductWarehouseRepository,
    private val warehouseOutboxRepository: WarehouseOutboxRepository,
    private val kafkaTemplate: KafkaTemplate<String, String>,
    warehouseConnectorConfiguration: Configuration
)
{
    private val executor: Executor = Executors.newSingleThreadExecutor()
    //Init a Debezium engine
    private var debeziumEngine: DebeziumEngine<RecordChangeEvent<SourceRecord>> =
        DebeziumEngine.create(ChangeEventFormat.of(Connect::class.java))
            .using(warehouseConnectorConfiguration.asProperties())
            .notifying { sourceRecordRecordChangeEvent: RecordChangeEvent<SourceRecord> ->
                //Set the function to run every time the debezium engine detects a change
                this.handleOutboxChangeEvent(sourceRecordRecordChangeEvent)
            }
            .build()

    @PostConstruct
    private fun startDebeziumEngine() = executor.execute(debeziumEngine)

    @PreDestroy
    private fun stopDebeziumEngine() = debeziumEngine.close()

    /* --------------------------- SAGA MANAGER --------------------------- */

    private fun rejectRequest(request: OrderEventRequest)
    {
        warehouseOutboxRepository.save(WarehouseOutbox.createRejectedWarehouseOutbox(request))
        println("[WAREHOUSE] REJECTED ${request.orderId}")
    }

    private fun acceptRequest(request: OrderEventRequest, warehouseId: Long)
    {
        //For each product, reduce the quantity
        request.productsList.forEach {
            pwRepository.reduceQuantity(it.quantity, warehouseId, it.productId)
            val pw = pwRepository.findByWarehouseIdAndProductId(warehouseId, it.productId)!!
            if (it.quantity < pw.alarmOnQuantity)
            {
                //If here, the quantity of this product in the warehouse is below a given threshold
                //So notify to Catalog to send an email to admins
                val message = QuantityAlarmEvent(it.productId, warehouseId)
                this.produceMessage(TO_CATALOG_TOPIC, message.toString())
            }
        }
        warehouseOutboxRepository.save(WarehouseOutbox.createAcceptedWarehouseOutbox(request, warehouseId))
        println("[WAREHOUSE] ACCEPTED ${request.orderId} at warehouse $warehouseId")
    }

    private fun compensateRequest(request: OrderEventRequest)
    {
        val orderRequest = warehouseOutboxRepository.findByOrderId(request.orderId) ?: return
        val warehouseId = orderRequest.warehouseId ?: throw RuntimeException("Logic error")
        //For each product, return it to the warehouse
        request.productsList.forEach { pwRepository.incrementQuantity(it.quantity, warehouseId, it.productId) }
        //Update outbox table
        orderRequest.compensate(request)
        warehouseOutboxRepository.save(orderRequest)
        println("[WAREHOUSE] COMPENSATED ${request.orderId} at warehouse $warehouseId")
    }

    private fun tryToFoundWarehouse(request: OrderEventRequest): Long?
    {
        var bIsFirst = true
        var totPrice = 0.0
        var warehousesSet: HashSet<Long> = hashSetOf()
        for (purchasedProduct in request.productsList)
        {
            //Check for errors in the request received from Order (since Order simply forwarded it)
            if (purchasedProduct.quantity <= 0)
                return null //Reject request
            val productId = purchasedProduct.productId
            val price = purchasedProduct.price
            val product = productRepository.findByProductIdAndPrice(productId, price.toBigDecimal()) ?: return null //Reject request
            if (product.name != purchasedProduct.name)
                return null //Reject request
            //Compute total price even server side and check for an error in the received total price
            totPrice += price * purchasedProduct.quantity
            if (totPrice > request.amount)
                return null //Reject request

            //Get all warehouses that can deploy the current product
            val warehouses = pwRepository.findAllByProductIdAndQuantityGreaterThanEqual(productId, purchasedProduct.quantity)
                .map{ it.warehouse.warehouseId!! }
            if (warehouses.isEmpty()) //Check if there is at least a warehouse that can deploy the current product
                return null //Reject request

            if (!bIsFirst)
            {
                //Compute the intersection between the current warehouses set and the previous one
                warehousesSet.retainAll(warehouses)
                if (warehousesSet.isEmpty()) //Check if there is at least a warehouse that can deploy all products examined so far
                    return null //Reject request
            }
            else
            {
                //If here, this is the first iteration of the for loop
                warehousesSet = warehouses.toHashSet()
                bIsFirst = false //The next iteration will be the second :)
            }
        }
        //Again, check for error in the request received from Order (since Order simply forwarded it)
        if (totPrice != request.amount)
            return null //Reject request

        return warehousesSet.first()
    }

    private fun handleMessageFromKafka(request: OrderEventRequest)
    {
        val furtherProcessing = this.checkIfAdditionalProcessingIsRequired(request) ?: return

        if (furtherProcessing == OrderStatusEvent.STARTED)
        {
            val warehouseId = this.tryToFoundWarehouse(request)
            if (warehouseId == null)
                this.rejectRequest(request)
            else
                this.acceptRequest(request, warehouseId)
        }
        else //i.e. furtherProcessing == OrderStatusEvent.COMPENSATING
            this.compensateRequest(request)
    }

    /* --------------------------- DEBEZIUM LISTENER AND OUTBOX PATTERN --------------------------- */

    private fun checkIfAdditionalProcessingIsRequired(request: OrderEventRequest): OrderStatusEvent?
    {
        val outboxEntry = warehouseOutboxRepository.findByOrderId(request.orderId)
        if (outboxEntry != null)
        {
            //If here, warehouse has already processed the request...
            if (outboxEntry.warehouseSagaStatus == OrderStatusEvent.REJECTED)
            {
                //If here, warehouse has already rejected the request.
                //So simply resend computed response
                this.produceMessage(TO_ORDER_TOPIC, outboxEntry.toOrderMsg)
                return null
            }
            if (request.status == OrderStatusEvent.STARTED)
            {
                //If here, request is asking me to start order processing...
                //However, if here, warehouse has already processed the request so simply resend computed response
                this.produceMessage(TO_ORDER_TOPIC, outboxEntry.toOrderMsg)
                return null
            }
            if (request.status == OrderStatusEvent.COMPENSATING && outboxEntry.warehouseSagaStatus == OrderStatusEvent.COMPENSATED)
            {
                //If here, request is asking me to compensate previous order processing...
                //However, if here, wallet has already compensated the request so simply resend computed response
                this.produceMessage(TO_ORDER_TOPIC, outboxEntry.toOrderMsg)
                return null
            }
        }
        else if (request.status == OrderStatusEvent.REJECTED || request.status == OrderStatusEvent.COMPENSATING)
        {
            //If here, wallet doesn't processed the request yet.
            //Nonetheless, request is asking to reject the order, so simply reject order.
            this.rejectRequest(request)
            return null
        }

        return if (request.status == OrderStatusEvent.STARTED) OrderStatusEvent.STARTED else OrderStatusEvent.COMPENSATING
    }

    private fun handleOutboxChangeEvent(sourceRecordRecordChangeEvent: RecordChangeEvent<SourceRecord>)
    {
        val sourceRecord = sourceRecordRecordChangeEvent.record()
        val sourceRecordChangeValue: Struct = sourceRecord.value() as Struct
        val operation: Envelope.Operation = Envelope.Operation.forCode(sourceRecordChangeValue.get(Envelope.FieldName.OPERATION) as String)

        //Handling only Update and Insert operations
        if (operation === Envelope.Operation.CREATE || operation === Envelope.Operation.UPDATE)
        {
            //Get info about table AFTER the operation performed on it
            val structAfter: Struct = sourceRecordChangeValue.get(Envelope.FieldName.AFTER) as Struct

            //Get messages to send
            val toOrderMsgAfter = structAfter[RESPONSE_TO_ORDER_CLN] as String

            //Send messages by kafka
            this.produceMessage(TO_ORDER_TOPIC, toOrderMsgAfter)
        }
    }

    /* --------------------------- KAFKA MANAGER --------------------------- */

    private fun produceMessage(topic: String, message: String)
    {
        println("[WAREHOUSE] sending $topic-$message")
        kafkaTemplate.send(topic, message)
    }

    @KafkaListener(topics = [FROM_ORDER_TOPIC])
    fun consumeFromOrder(message: String)
    {
        val request = OrderEventRequest.createByString(message)
        println("[WAREHOUSE] received $request")
        this.handleMessageFromKafka(request)
    }
}
