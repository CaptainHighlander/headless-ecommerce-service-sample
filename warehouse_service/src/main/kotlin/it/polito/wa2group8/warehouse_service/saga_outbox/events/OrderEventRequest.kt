package it.polito.wa2group8.warehouse_service.saga_outbox.events

import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.databind.ObjectMapper

data class PurchasedProductDTO (
    @JsonProperty("productId") val productId: Long,
    @JsonProperty("name") val name: String,
    @JsonProperty("quantity") val quantity: Int,
    @JsonProperty("price") val price: Double,
)

/**
 * Represents a message received by the microservice "Order" after an order request by microservice "Catalog"
 */
data class OrderEventRequest(
    @JsonProperty("orderId") val orderId: Long,
    @JsonProperty("amount") val amount: Double,
    @JsonProperty("status") val status: OrderStatusEvent,
    @JsonProperty("productsList") val productsList: List<PurchasedProductDTO>,
)
{
    companion object
    {
        private val objectMapper = ObjectMapper()
        fun createByString(message: String): OrderEventRequest = objectMapper.readValue(message, OrderEventRequest::class.java)
    }
}
