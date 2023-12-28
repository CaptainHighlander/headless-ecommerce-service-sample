package it.polito.wa2group8.warehouse_service.dto

import javax.validation.constraints.Min

data class ProductWarehouseInput(
    @get:Min(value = 0, message = "ProductID cannot be lower than 0")
    val productID: Long,
    val quantity: Int,
    @get:Min(value = 1, message = "Alarm on quantity cannot be lower than 1")
    val alarmOnQuantity: Int?,
)

data class WarehouseProductsDTO(
    val products: Set<ProductWarehouseInput>
)
