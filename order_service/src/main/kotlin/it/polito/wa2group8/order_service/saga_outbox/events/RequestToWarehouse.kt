package it.polito.wa2group8.order_service.saga_outbox.events

import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.databind.ObjectMapper
import it.polito.wa2group8.order_service.dto.PurchasedProductDTO

/**
 * Represents a message to send to the microservice "Warehouse" after an order request by microservice "Catalog"
 */
data class RequestToWarehouse(
    @JsonProperty("orderId") val orderId: Long,
    @JsonProperty("amount") val amount: Double,
    @JsonProperty("status") val status: OrderStatusEvent,
    @JsonProperty("productsList") val productsList: List<PurchasedProductDTO>,
)
{
    private val objectMapper = ObjectMapper()
    override fun toString(): String = objectMapper.writeValueAsString(this)
}
