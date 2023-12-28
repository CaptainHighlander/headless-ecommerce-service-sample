package it.polito.wa2group8.order_service.saga_outbox.events

import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.databind.ObjectMapper

/**
 * Represents a message to send to the microservice "Wallet" after an order request by microservice "Catalog"
 */
data class RequestToWallet(
    @JsonProperty("orderId") val orderId: Long,
    @JsonProperty("username") val username: String,
    @JsonProperty("amount") val amount: Double,
    @JsonProperty("status") val status: OrderStatusEvent
)
{
    private val objectMapper = ObjectMapper()
    override fun toString(): String = objectMapper.writeValueAsString(this)
}
