package it.polito.wa2group8.order_service.dto


import it.polito.wa2group8.order_service.domain.Delivery

//Represents the Delivery object to expose to GraphQL
data class DeliveryDTO(
    val warehouseId: Long?,
    val city: String?,
    val street: String?,
    val zip: String?,
)

//Converts a Delivery object to a DeliveryDTO object
fun Delivery.toDeliveryDTO() = DeliveryDTO(warehouseId, city, street, zip)
