package it.polito.wa2group8.wallet_service.saga_outbox

import io.debezium.config.Configuration
import io.debezium.data.Envelope
import io.debezium.embedded.Connect
import io.debezium.engine.DebeziumEngine
import io.debezium.engine.RecordChangeEvent
import io.debezium.engine.format.ChangeEventFormat
import it.polito.wa2group8.wallet_service.domain.*
import it.polito.wa2group8.wallet_service.repositories.CustomerRepository
import it.polito.wa2group8.wallet_service.repositories.TransactionRepository
import it.polito.wa2group8.wallet_service.repositories.WalletOutboxRepository
import it.polito.wa2group8.wallet_service.saga_outbox.events.*
import it.polito.wa2group8.wallet_service.repositories.WalletRepository
import org.apache.kafka.connect.data.Struct
import org.apache.kafka.connect.source.SourceRecord
import org.springframework.data.repository.findByIdOrNull
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import java.math.BigDecimal
import java.time.LocalDateTime
import java.util.concurrent.Executor
import java.util.concurrent.Executors
import javax.annotation.PostConstruct
import javax.annotation.PreDestroy

/**
 * This class handle orders (wallet side) according patterns Saga and Outbox
 */

@Transactional
@Component
class WalletSagaManager(
    private val walletRepository: WalletRepository,
    private val transactionRepository: TransactionRepository,
    private val walletOutboxRepository: WalletOutboxRepository,
    private val customerRepository: CustomerRepository,
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

    fun rejectRequest(request: OrderEventRequest)
    {
        walletOutboxRepository.save(WalletOutbox.createRejectedWalletOutbox(request))
        println("[WALLET] REJECTED ${request.orderId}")
    }

    fun acceptRequest(request: OrderEventRequest, wallet: Wallet)
    {
        //Perform transaction
        doTransaction(wallet, TransactionReason.ORDER_PLACEMENT, request.amount.toBigDecimal())
        //Update outbox table
        walletOutboxRepository.save(WalletOutbox.createAcceptedWalletOutbox(request, wallet.getId()!!))
        println("[WALLET] ACCEPTED ${request.orderId}")
    }

    fun compensateRequest(request: OrderEventRequest)
    {
        val outboxOrder = walletOutboxRepository.findByOrderId(request.orderId) ?: return
        val walletId = outboxOrder.walletId ?: throw RuntimeException("logic error")
        val wallet = walletRepository.findByIdOrNull(walletId) ?: throw RuntimeException("logic error")
        //Perform transaction
        doTransaction(wallet, TransactionReason.ORDER_RECHARGE, request.amount.toBigDecimal())
        //Update outbox table
        outboxOrder.compensate(request)
        walletOutboxRepository.save(outboxOrder)
        println("[WALLET] COMPENSATED ${request.orderId}")
    }

    private fun doTransaction(userWallet: Wallet, reason: TransactionReason, amount: BigDecimal)
    {
        //update customer's wallet
        if (reason == TransactionReason.ORDER_RECHARGE)
            userWallet.currentAmount += amount //Refund user
        else
            userWallet.currentAmount -= amount //Detract money
        walletRepository.updateAmount(userWallet.currentAmount, userWallet.getId()!!)
        //Update company's wallet
        val companyWallet = walletRepository.findFirstByCustomerUsername("COMPANY")
        if (companyWallet != null)
        {
            if (reason == TransactionReason.ORDER_RECHARGE)
                companyWallet.currentAmount -= amount
            else
                companyWallet.currentAmount += amount
            walletRepository.updateAmount(companyWallet.currentAmount, companyWallet.getId()!!)
            val transaction = if (reason == TransactionReason.ORDER_RECHARGE)
                Transaction(amount, LocalDateTime.now(), companyWallet, userWallet, reason)
            else
                Transaction(amount, LocalDateTime.now(), userWallet, companyWallet, reason)
            transactionRepository.save(transaction)
        }
    }

    fun handleMessageFromKafka(request: OrderEventRequest)
    {
        val furtherProcessing = this.checkIfAdditionalProcessingIsRequired(request) ?: return
        //If here, request requires further processing

        if (furtherProcessing == OrderStatusEvent.STARTED)
        {
            val customer = customerRepository.findByUsername(request.username)
            if (customer == null) //Customer doesn't have any wallet
            {
                if (request.status == OrderStatusEvent.COMPENSATING)
                    throw RuntimeException("Logic error in wallet detected in handleMessageFromKafka")
                this.rejectRequest(request)
                return
            }
            val wallet = walletRepository.findFirstByCustomerAndCurrentAmountGreaterThanEqual(customer, request.amount.toBigDecimal())
            if (wallet != null)
                acceptRequest(request, wallet)
            else
                this.rejectRequest(request)
        }
        else //i.e. furtherProcessing == OrderStatusEvent.COMPENSATING
            this.compensateRequest(request)
    }

    /* --------------------------- DEBEZIUM LISTENER AND OUTBOX PATTERN --------------------------- */

    private fun checkIfAdditionalProcessingIsRequired(request: OrderEventRequest): OrderStatusEvent?
    {
        val outboxEntry = walletOutboxRepository.findByOrderId(request.orderId)
        if (outboxEntry != null)
        {
            //If here, wallet has already processed the request...
            if (outboxEntry.walletSagaStatus == OrderStatusEvent.REJECTED)
            {
                //If here, wallet has already rejected the request.
                //So simply resend computed response
                this.produceMessage(TO_ORDER_TOPIC, outboxEntry.toOrderMsg)
                return null
            }
            if (request.status == OrderStatusEvent.STARTED)
            {
                //If here, request is asking me to start order processing...
                //However, if here, wallet has already processed the request so simply resend computed response
                this.produceMessage(TO_ORDER_TOPIC, outboxEntry.toOrderMsg)
                return null
            }
            if (request.status == OrderStatusEvent.COMPENSATING && outboxEntry.walletSagaStatus == OrderStatusEvent.COMPENSATED)
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
        println("[WALLET] sending $topic-$message")
        kafkaTemplate.send(topic, message)
    }

    @KafkaListener(topics = [FROM_ORDER_TOPIC])
    fun consumeFromOrder(message: String)
    {
        val request = OrderEventRequest.createByString(message)
        println("[WALLET] received $request")
        this.handleMessageFromKafka(request)
    }
}
