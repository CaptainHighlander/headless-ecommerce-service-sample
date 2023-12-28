package it.polito.wa2group8.order_service.saga_outbox.events

import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.databind.ObjectMapper
import it.polito.wa2group8.order_service.domain.OrderStatus

/**
 * Represents a message to send to the microservice "Catalog" when an order changes is status
 */
data class EmailEvent(
    @JsonProperty("orderId") val orderId: Long,
    @JsonProperty("username") val username: String,
    @JsonProperty("status") val status: OrderStatus,
)
{
    private val objectMapper = ObjectMapper()
    override fun toString(): String = objectMapper.writeValueAsString(this)
}
