package it.polito.wa2group8.wallet_service.saga_outbox.events

import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.databind.ObjectMapper

/**
 * Represents a message received by the microservice "Order" after an order request by microservice "Catalog"
 */
data class OrderEventRequest(
    @JsonProperty("orderId") val orderId: Long,
    @JsonProperty("username") val username: String,
    @JsonProperty("amount") val amount: Double,
    @JsonProperty("status") val status: OrderStatusEvent
)
{
    companion object
    {
        private val objectMapper = ObjectMapper()
        fun createByString(message: String): OrderEventRequest = objectMapper.readValue(message, OrderEventRequest::class.java)
    }
}
