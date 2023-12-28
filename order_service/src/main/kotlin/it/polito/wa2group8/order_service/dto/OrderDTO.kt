package it.polito.wa2group8.order_service.dto

import it.polito.wa2group8.order_service.domain.Order
import it.polito.wa2group8.order_service.domain.OrderStatus

//Represents the Order object to expose to GraphQL
data class OrderDTO (
    val orderId: Long,
    val buyerId: String?,
    val purchasePrice: Double?,
    val orderStatus: OrderStatus,
)
{
    var productsList: List<PurchasedProductDTO>? = null
    var delivery: DeliveryDTO? = null
}

//Converts a Order object to an OrderDTO object
fun Order.toOrderDTO() = OrderDTO(orderId ?: -1, buyerId, purchasePrice, orderStatus)
