package it.polito.wa2group8.warehouse_service.saga_outbox.events

import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.databind.ObjectMapper

/**
 * Represents a message to send to the microservice "Order" as a reply to an OrderEventRequest
 */
data class OrderEventResponse(
    @JsonProperty("orderId") val orderId: Long,
    @JsonProperty("status") val status: OrderStatusEvent,
    @JsonProperty("warehouseId") val warehouseId: Long,
)
{
    private val objectMapper = ObjectMapper()
    override fun toString(): String = objectMapper.writeValueAsString(this)
}