package it.polito.wa2group8.wallet_service.domain

import it.polito.wa2group8.wallet_service.saga_outbox.events.OrderEventRequest
import it.polito.wa2group8.wallet_service.saga_outbox.events.OrderEventResponse
import it.polito.wa2group8.wallet_service.saga_outbox.events.OrderStatusEvent
import org.springframework.beans.factory.annotation.Value
import javax.persistence.*

const val RESPONSE_TO_ORDER_CLN = "to_order_msg"
const val WALLET_SAGA_STATUS_CLN = "wallet_saga_status"

@Value("\${spring.datasource.outbox-table}")
const val outbox_table_name = ""

@Table(name = outbox_table_name)
@Entity
class WalletOutbox(
    @Column(nullable = false, name = "order_id", unique = true)
    val orderId: Long,

    @Column(name = RESPONSE_TO_ORDER_CLN, nullable = false)
    var toOrderMsg: String,

    @Column(name = WALLET_SAGA_STATUS_CLN, nullable = false)
    @Enumerated(EnumType.STRING)
    var walletSagaStatus: OrderStatusEvent,

    var walletId: Long?,
): EntityBase<Long>()
{
    companion object
    {
        fun createRejectedWalletOutbox(request: OrderEventRequest) : WalletOutbox
        {
            val response = OrderEventResponse(request.orderId, OrderStatusEvent.REJECTED, -1)
            return WalletOutbox(response.orderId, response.toString(), response.status, null)
        }

        fun createAcceptedWalletOutbox(request: OrderEventRequest, walletId: Long) : WalletOutbox
        {
            val response = OrderEventResponse(request.orderId, OrderStatusEvent.ACCEPTED, -1)
            return WalletOutbox(response.orderId, response.toString(), response.status, walletId)
        }
    }

    fun compensate(request: OrderEventRequest)
    {
        val response = OrderEventResponse(request.orderId, OrderStatusEvent.COMPENSATED, -1)
        this.toOrderMsg = response.toString()
        this.walletSagaStatus = response.status
    }
}
