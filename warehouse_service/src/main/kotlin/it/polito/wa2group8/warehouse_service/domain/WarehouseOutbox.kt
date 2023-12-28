package it.polito.wa2group8.warehouse_service.domain

import it.polito.wa2group8.warehouse_service.saga_outbox.events.OrderEventRequest
import it.polito.wa2group8.warehouse_service.saga_outbox.events.OrderEventResponse
import it.polito.wa2group8.warehouse_service.saga_outbox.events.OrderStatusEvent
import org.springframework.beans.factory.annotation.Value
import javax.persistence.*

const val RESPONSE_TO_ORDER_CLN = "to_order_msg"
const val WAREHOUSE_SAGA_STATUS_CLN = "warehouse_saga_status"

@Value("\${spring.datasource.outbox-table}")
const val outbox_table_name = ""

@Table(name = outbox_table_name)
@Entity
class WarehouseOutbox(
    @Id
    @GeneratedValue(strategy=GenerationType.AUTO)
    var id: Long?,

    @Column(nullable = false, name = "order_id", unique = true)
    val orderId: Long,

    @Column(name = RESPONSE_TO_ORDER_CLN, nullable = false)
    var toOrderMsg: String,

    @Column(name = WAREHOUSE_SAGA_STATUS_CLN, nullable = false)
    @Enumerated(EnumType.STRING)
    var warehouseSagaStatus: OrderStatusEvent,

    var warehouseId: Long?
)
{
    companion object
    {
        fun createRejectedWarehouseOutbox(request: OrderEventRequest) : WarehouseOutbox
        {
            val response = OrderEventResponse(request.orderId, OrderStatusEvent.REJECTED, -1)
            return WarehouseOutbox(null, response.orderId, response.toString(), response.status, null)
        }

        fun createAcceptedWarehouseOutbox(request: OrderEventRequest, warehouseId: Long) : WarehouseOutbox
        {
            val response = OrderEventResponse(request.orderId, OrderStatusEvent.ACCEPTED, warehouseId)
            return WarehouseOutbox(null, response.orderId, response.toString(), response.status, warehouseId)
        }
    }

    fun compensate(request: OrderEventRequest)
    {
        val response = OrderEventResponse(request.orderId, OrderStatusEvent.COMPENSATED, this.warehouseId ?: -1)
        this.toOrderMsg = response.toString()
        this.warehouseSagaStatus = response.status
    }
}
