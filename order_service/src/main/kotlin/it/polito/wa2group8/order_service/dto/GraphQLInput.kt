package it.polito.wa2group8.order_service.dto

import it.polito.wa2group8.order_service.domain.OrderStatus

//Represents the PurchasedProductInput object to expose to GraphQL (in order to create a PurchasedProduct)
data class PurchasedProductInput(val productId: Long, val name: String, val price: Double, val quantity: Int)

//Represents the OrderInput object to expose to GraphQL (in order to create an Order)
data class OrderInput(val products: List<PurchasedProductInput>, val purchasePrice: Double)

//Represents the OrderUpdateInput object to expose to GraphQL (in order to update the status of an Order)
data class OrderUpdateInput(val orderId: Long, val orderStatus: OrderStatus)
