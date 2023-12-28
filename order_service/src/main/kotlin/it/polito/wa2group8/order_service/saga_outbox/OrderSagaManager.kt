package it.polito.wa2group8.order_service.saga_outbox

import io.debezium.config.Configuration
import io.debezium.data.Envelope
import io.debezium.embedded.Connect
import io.debezium.engine.DebeziumEngine
import io.debezium.engine.RecordChangeEvent
import io.debezium.engine.format.ChangeEventFormat
import it.polito.wa2group8.order_service.domain.*
import it.polito.wa2group8.order_service.dto.OrderInput
import it.polito.wa2group8.order_service.dto.UserInfoDTO
import it.polito.wa2group8.order_service.dto.toPurchasedProductDTO
import it.polito.wa2group8.order_service.exceptions.NotFoundException
import it.polito.wa2group8.order_service.repositories.*
import it.polito.wa2group8.order_service.saga_outbox.events.*
import org.apache.kafka.connect.data.Struct
import org.apache.kafka.connect.source.SourceRecord
import org.springframework.data.repository.findByIdOrNull
import org.springframework.kafka.annotation.EnableKafka
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.scheduling.annotation.EnableScheduling
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.concurrent.Executor
import java.util.concurrent.Executors
import javax.annotation.PostConstruct
import javax.annotation.PreDestroy

/**
 * This class handle orders according patterns Saga and Outbox
 */
@Service
@EnableScheduling
@Transactional
@EnableKafka
class OrderSagaManager(
    private val orderRepository: OrderRepository,
    private val purchasedProductRepository: PurchasedProductRepository,
    private val deliveryRepository: DeliveryRepository,
    private val orderOutboxRepository: OrderOutboxRepository,
    private val kafkaTemplate: KafkaTemplate<String, String>,
    orderConnectorConfiguration: Configuration
)
{
    private val executor: Executor = Executors.newSingleThreadExecutor()
    //Init a Debezium engine
    private var debeziumEngine: DebeziumEngine<RecordChangeEvent<SourceRecord>> =
        DebeziumEngine.create(ChangeEventFormat.of(Connect::class.java))
            .using(orderConnectorConfiguration.asProperties())
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

    @Scheduled(fixedRate = 45000)
    fun retryRequest()
    {
        val pendingOrders = orderOutboxRepository.findAllByOrderSagaStatus(OrderStatusEvent.STARTED)
        if (pendingOrders.count() >= 1)
        {
            pendingOrders.forEach { order ->
                if (order.walletSagaStatus == OrderStatusEvent.STARTED || order.walletSagaStatus == OrderStatusEvent.COMPENSATING)
                    this.produceMessage(TO_WALLET_TOPIC, order.toWalletMsg) //Resend request to microservice "Wallet"
                if (order.warehouseSagaStatus == OrderStatusEvent.STARTED || order.warehouseSagaStatus == OrderStatusEvent.COMPENSATING)
                    this.produceMessage(TO_WAREHOUSE_TOPIC, order.toWarehouseMsg) //Resend request to microservice "Warehouse"
            }
        }
    }

    fun newOrder(orderInput: OrderInput, userInfoDTO: UserInfoDTO): Long?
    {
        //Creation of a new entry for the current order
        val newOrder = orderRepository.save(Order(null, userInfoDTO.username, orderInput.purchasePrice))
        val orderId = newOrder.orderId!!

        //Creation of an entry in the database for each product associated to the order
        val products = orderInput.products.map { it.toPurchaseProduct(orderId) }
        products.forEach{ purchasedProductRepository.save(it) }

        //Creation of the delivery entry (missing of warehouseId, for the moment)
        deliveryRepository.save(Delivery(null, orderId, userInfoDTO.city, userInfoDTO.street, userInfoDTO.zip))

        //Creation of two different events to send respectively to the microservice "Wallet" and to the microservice "Warehouse"
        val requestToWallet = RequestToWallet(
            orderId, userInfoDTO.username, orderInput.purchasePrice, OrderStatusEvent.STARTED
        )
        val requestToWarehouse = RequestToWarehouse(
            orderId, orderInput.purchasePrice, OrderStatusEvent.STARTED,
            orderInput.products.map{ it.toPurchasedProductDTO() }
        )

        //Creation of the outbox entry
        val orderOutbox = OrderOutbox(
            null, orderId,
            OrderStatusEvent.STARTED, requestToWallet.toString(),
            OrderStatusEvent.STARTED, requestToWarehouse.toString(),
            OrderStatusEvent.STARTED
        )
        orderOutboxRepository.save(orderOutbox)
        return newOrder.orderId
    }

    fun deleteOrder(order: Order)
    {
        val orderId = order.orderId!!
        //Updating of the order status (now is CANCELED)
        order.orderStatus = OrderStatus.CANCELED
        orderRepository.updateStatus(order.orderStatus, orderId)

        //Mark as rejected all the status
        val outboxOrder = orderOutboxRepository.findByOrderId(orderId)!!
        outboxOrder.orderSagaStatus = OrderStatusEvent.STARTED
        outboxOrder.walletSagaStatus = OrderStatusEvent.COMPENSATING
        outboxOrder.warehouseSagaStatus = OrderStatusEvent.COMPENSATING
        val requestToWarehouse = RequestToWarehouse(
            outboxOrder.orderId, order.purchasePrice, outboxOrder.warehouseSagaStatus,
            purchasedProductRepository.findAllByOrderId(orderId).map{ it.toPurchasedProductDTO() }
        )
        outboxOrder.toWarehouseMsg = requestToWarehouse.toString()
        val requestToWallet = RequestToWallet(
            outboxOrder.orderId, order.buyerId, order.purchasePrice, outboxOrder.walletSagaStatus
        )
        outboxOrder.toWalletMsg = requestToWallet.toString()

        //Update the outbox table
        orderOutboxRepository.save(outboxOrder)
    }

    private fun handleMessageFromKafka(response: Response, fromWallet: Boolean)
    {
        val order = orderRepository.findByIdOrNull(response.orderId) ?: throw NotFoundException("Order id missing")
        val outboxOrder = orderOutboxRepository.findByOrderId(response.orderId) ?: throw NotFoundException("Order id missing")
        if (outboxOrder.orderSagaStatus == OrderStatusEvent.FINISHED)
            return //If the saga phase for this order is finished, ignore the response received from the microservice "Wallet"

        //Update external microservice status on the outbox order
        if (fromWallet)
            outboxOrder.walletSagaStatus = response.status
        else //i.e. response from warehouse
        {
            outboxOrder.warehouseSagaStatus = response.status
            if (outboxOrder.warehouseSagaStatus == OrderStatusEvent.ACCEPTED)
                outboxOrder.warehouseId = response.warehouseId
        }

        //External microservice rejected the transaction
        if (response.status == OrderStatusEvent.REJECTED)
        {
            if (fromWallet) //i.e. negative response from microservice "Wallet"
            {
                val status = outboxOrder.warehouseSagaStatus
                if (status != OrderStatusEvent.REJECTED && status != OrderStatusEvent.COMPENSATED)
                {
                    //Microservice "Warehouse" had already confirmed the order
                    //So we have to tell him that the order has to cancelled/compensate
                    outboxOrder.warehouseSagaStatus = OrderStatusEvent.COMPENSATING
                    val requestToWarehouse = RequestToWarehouse(
                        outboxOrder.orderId, order.purchasePrice, outboxOrder.warehouseSagaStatus,
                        purchasedProductRepository.findAllByOrderId(response.orderId).map{ it.toPurchasedProductDTO() }
                    )
                    outboxOrder.toWarehouseMsg = requestToWarehouse.toString()
                }
            }
            else //i.e. negative response from microservice "Warehouse"
            {
                val status = outboxOrder.walletSagaStatus
                if (status != OrderStatusEvent.REJECTED && status != OrderStatusEvent.COMPENSATED)
                {
                    //Microservice "Wallet" had already confirmed the order
                    //So we have to tell him that the order has to be cancelled/compensate
                    outboxOrder.walletSagaStatus = OrderStatusEvent.COMPENSATING
                    val requestToWallet = RequestToWallet(
                        outboxOrder.orderId, order.buyerId, order.purchasePrice, outboxOrder.walletSagaStatus
                    )
                    outboxOrder.toWalletMsg = requestToWallet.toString()
                }
            }
        }

        //Check if SAGA is ended and, if so, finalize order
        finalizeOrder(outboxOrder, order)

        //Finally, update outbox table in order to manage the sending of messages via kafka
        orderOutboxRepository.save(outboxOrder)
    }

    private fun finalizeOrder(outboxOrder: OrderOutbox, order: Order)
    {
        val condition1 = outboxOrder.walletSagaStatus == OrderStatusEvent.ACCEPTED && outboxOrder.warehouseSagaStatus == OrderStatusEvent.ACCEPTED
        val condition2 = outboxOrder.walletSagaStatus == OrderStatusEvent.REJECTED && outboxOrder.warehouseSagaStatus == OrderStatusEvent.REJECTED
        val condition3 = outboxOrder.walletSagaStatus == OrderStatusEvent.REJECTED && outboxOrder.warehouseSagaStatus == OrderStatusEvent.COMPENSATED
        val condition4 = outboxOrder.walletSagaStatus == OrderStatusEvent.COMPENSATED && outboxOrder.warehouseSagaStatus == OrderStatusEvent.REJECTED
        val condition5 = outboxOrder.walletSagaStatus == OrderStatusEvent.COMPENSATED && outboxOrder.warehouseSagaStatus == OrderStatusEvent.COMPENSATED
        val endCondition = condition1 || condition2 || condition3 || condition4 || condition5
        //Check if SAGA is ended. If not, return
        if (!endCondition)
            return

        //Set Saga status as FINISHED
        outboxOrder.orderSagaStatus = OrderStatusEvent.FINISHED

        //Check if order can be issued or if is failed
        if (outboxOrder.isConfirmed())
        {
            //Both microservices "Wallet" and "Warehouse" confirmed order. So mark order as ISSUED.
            order.orderStatus = OrderStatus.ISSUED
            //Fill the field warehouse id of the delivery address and update it on the DB
            outboxOrder.warehouseId ?: throw RuntimeException("Logic error")
            deliveryRepository.updateWarehouseId(outboxOrder.warehouseId, outboxOrder.orderId)
        }
        else if(order.orderStatus != OrderStatus.CANCELED)
            order.orderStatus = OrderStatus.FAILED

        //Update order status
        orderRepository.updateStatus(order.orderStatus, order.orderId!!)
    }

    /* --------------------------- DEBEZIUM LISTENER AND OUTBOX PATTERN --------------------------- */

    private fun handleOutboxChangeEvent(sourceRecordRecordChangeEvent: RecordChangeEvent<SourceRecord>)
    {
        val sourceRecord = sourceRecordRecordChangeEvent.record()
        val sourceRecordChangeValue: Struct = sourceRecord.value() as Struct
        val operation: Envelope.Operation = Envelope.Operation.forCode(sourceRecordChangeValue.get(Envelope.FieldName.OPERATION) as String)

        //Handling only Update and Insert operations
        if (operation !== Envelope.Operation.CREATE && operation !== Envelope.Operation.UPDATE)
            return

        //Get info about table AFTER the operation performed on it
        val structAfter: Struct = sourceRecordChangeValue.get(Envelope.FieldName.AFTER) as Struct

        //Check if AFTER the operation the order is completed
        val orderSagaStatus = structAfter[ORDER_SAGA_STATUS_CLN] as String
        if (orderSagaStatus == OrderStatusEvent.FINISHED.toString())
            return

        if (operation === Envelope.Operation.CREATE)
        {
            //Get messages to send
            val toWalletMsgAfter = structAfter[REQUEST_TO_WALLET_CLN] as String
            val toWarehouseMsgAfter = structAfter[REQUEST_TO_WAREHOUSE_CLN] as String

            //Send messages via kafka both to Wallet and to Warehouse
            this.produceMessage(TO_WALLET_TOPIC, toWalletMsgAfter)
            this.produceMessage(TO_WAREHOUSE_TOPIC, toWarehouseMsgAfter)
        }
        else //i.e. (operation === Envelope.Operation.UPDATE)
        {
            //Get info about table BEFORE the operation performed on it
            val structBefore: Struct = sourceRecordChangeValue.get(Envelope.FieldName.BEFORE) as Struct

            //Get status for wallet and warehouse BEFORE and AFTER the update
            val walletStatusBefore = structBefore[WALLET_SAGA_STATUS_CLN] as String
            val walletStatusAfter = structAfter[WALLET_SAGA_STATUS_CLN] as String
            val warehouseStatusBefore = structBefore[WAREHOUSE_SAGA_STATUS_CLN] as String
            val warehouseStatusAfter = structAfter[WAREHOUSE_SAGA_STATUS_CLN] as String

            //Get messages to send
            val toWalletMsgAfter = structAfter[REQUEST_TO_WALLET_CLN] as String
            val toWarehouseMsgAfter = structAfter[REQUEST_TO_WAREHOUSE_CLN] as String

            //Check if notify someone via kafka
            if (
                walletStatusAfter != walletStatusBefore &&
                walletStatusAfter != OrderStatusEvent.ACCEPTED.toString() &&
                walletStatusAfter != OrderStatusEvent.COMPENSATED.toString() &&
                warehouseStatusAfter != OrderStatusEvent.REJECTED.toString()
            )   this.produceMessage(TO_WAREHOUSE_TOPIC, toWarehouseMsgAfter)
            if (
                warehouseStatusBefore != warehouseStatusAfter &&
                warehouseStatusAfter != OrderStatusEvent.ACCEPTED.toString() &&
                warehouseStatusAfter != OrderStatusEvent.COMPENSATED.toString() &&
                walletStatusAfter != OrderStatusEvent.REJECTED.toString()
            )   this.produceMessage(TO_WALLET_TOPIC, toWalletMsgAfter)
        }
    }

    /* --------------------------- KAFKA MANAGER --------------------------- */

    private fun produceMessage(topic: String, message: String)
    {
        println("[ORDER] sending $topic-$message")
        kafkaTemplate.send(topic, message)
    }

    @KafkaListener(topics = [FROM_WALLET_TOPIC])
    fun consumeFromWallet(message: String)
    {
        //Get response
        val response = Response.createByString(message)
        println("responseFromWallet: $response")
        this.handleMessageFromKafka(response, true)
    }

    @KafkaListener(topics = [FROM_WAREHOUSE_TOPIC])
    fun consumeFromWarehouse(message: String)
    {
        //Get response
        val response = Response.createByString(message)
        println("responseFromWarehouse: $response")
        this.handleMessageFromKafka(response, false)
    }
}
