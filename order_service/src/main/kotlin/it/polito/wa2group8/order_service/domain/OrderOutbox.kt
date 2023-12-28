package it.polito.wa2group8.order_service.domain

import it.polito.wa2group8.order_service.saga_outbox.events.OrderStatusEvent
import org.springframework.beans.factory.annotation.Value
import javax.persistence.*

const val WALLET_SAGA_STATUS_CLN = "wallet_saga_status"
const val REQUEST_TO_WALLET_CLN = "to_wallet_msg"
const val WAREHOUSE_SAGA_STATUS_CLN = "warehouse_saga_status"
const val REQUEST_TO_WAREHOUSE_CLN = "to_warehouse_msg"
const val ORDER_SAGA_STATUS_CLN = "order_saga_status"

@Value("\${spring.r2dbc.outbox-table}")
const val outbox_table_name = ""

@Table(name = outbox_table_name)
@Entity
class OrderOutbox(
    @Id
    @GeneratedValue(strategy= GenerationType.AUTO)
    val id: Long? = null,

    @Column(name = "order_id", unique = true, nullable = false)
    val orderId: Long,

    @Column(name = WALLET_SAGA_STATUS_CLN, nullable = false)
    @Enumerated(EnumType.STRING)
    var walletSagaStatus: OrderStatusEvent,

    @Column(name = REQUEST_TO_WALLET_CLN, nullable = false, length = 500)
    var toWalletMsg: String,

    @Column(name = WAREHOUSE_SAGA_STATUS_CLN, nullable = false)
    @Enumerated(EnumType.STRING)
    var warehouseSagaStatus: OrderStatusEvent,

    @Column(name = REQUEST_TO_WAREHOUSE_CLN, nullable = false, length = 5000)
    var toWarehouseMsg: String,

    @Column(name = ORDER_SAGA_STATUS_CLN, nullable = false)
    @Enumerated(EnumType.STRING)
    var orderSagaStatus: OrderStatusEvent,

    var warehouseId: Long? = null,
)
{
    fun isConfirmed(): Boolean
    {
        return (walletSagaStatus == OrderStatusEvent.ACCEPTED && warehouseSagaStatus == OrderStatusEvent.ACCEPTED)
    }
}
