package it.polito.wa2group8.order_service.dto

import it.polito.wa2group8.order_service.domain.PurchasedProduct

//Represents the PurchasedProduct object to expose to GraphQL
data class PurchasedProductDTO (
    val productId: Long,
    val name: String,
    val quantity: Int,
    val price: Double,
)

//Converts a PurchasedProduct object to a PurchasedProductDTO object
fun PurchasedProduct.toPurchasedProductDTO() = PurchasedProductDTO(productId, name, quantity, price)

//Converts a PurchasedProductInput object to a PurchasedProductDTO object
fun PurchasedProductInput.toPurchasedProductDTO() = PurchasedProductDTO(productId, name, quantity, price)
