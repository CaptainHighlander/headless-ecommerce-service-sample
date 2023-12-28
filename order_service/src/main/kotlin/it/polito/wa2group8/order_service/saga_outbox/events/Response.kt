package it.polito.wa2group8.order_service.saga_outbox.events

import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.databind.ObjectMapper

/**
 * Represents a response received by the microservices "Wallet" or "Warehouse"
 */
data class Response(
    @JsonProperty("orderId") val orderId: Long,
    @JsonProperty("status") val status: OrderStatusEvent,
    @JsonProperty("warehouseId") val warehouseId: Long,
)
{
    companion object
    {
        private val objectMapper = ObjectMapper()
        fun createByString(message: String): Response = objectMapper.readValue(message, Response::class.java)
    }
}