package it.polito.wa2group8.catalog_service.dto

import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.databind.ObjectMapper

const val FROM_ORDER_TOPIC = "orderStatusChanges"
const val FROM_WAREHOUSE_TOPIC = "quantityAlarmLevel"

enum class OrderStatus
{
    PENDING,        //Order needs to be validated by microservices "Wallet" and "Warehouse" (NOT USED HERE)
    ISSUED,         //Order accepted and paid, ready to be delivered
    DELIVERING,     //The items have left the warehouse
    DELIVERED,      //Successful order
    FAILED,         //Order was failed (an order can fail in any moment)
    CANCELED,       //Order was canceled by the user. Cancel requests are considered only if the status is Issued
}

/**
 * Represents a message received from the microservice "Order" when an order changes is status
 */
data class EmailEvent(
    @JsonProperty("orderId") val orderId: Long,
    @JsonProperty("username") val username: String,
    @JsonProperty("status") val status: OrderStatus,
)
{
    companion object
    {
        private val objectMapper = ObjectMapper()
        fun createByString(message: String): EmailEvent = objectMapper.readValue(message, EmailEvent::class.java)
    }
}

/**
 * Represents a message received from the microservice "Warehouse" when
 * the quantity of the product in a certain warehouse is below a certain threshold
 */
data class QuantityAlarmEvent(
    @JsonProperty("productId") val productId: Long,
    @JsonProperty("warehouseId") val warehouseId: String,
)
{
    companion object
    {
        private val objectMapper = ObjectMapper()
        fun createByString(message: String): QuantityAlarmEvent = objectMapper.readValue(message, QuantityAlarmEvent::class.java)
    }
}
