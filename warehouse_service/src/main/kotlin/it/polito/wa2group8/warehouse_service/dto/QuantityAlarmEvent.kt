package it.polito.wa2group8.warehouse_service.dto

import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.databind.ObjectMapper

const val TO_CATALOG_TOPIC = "quantityAlarmLevel"

/**
 * Represents a message to send to the microservice "Catalog" when
 * the quantity of the product in a certain warehouse is below a certain threshold
 */
data class QuantityAlarmEvent(
    @JsonProperty("productId") val productId: Long,
    @JsonProperty("warehouseId") val warehouseId: Long,
)
{
    private val objectMapper = ObjectMapper()
    override fun toString(): String = objectMapper.writeValueAsString(this)
}
