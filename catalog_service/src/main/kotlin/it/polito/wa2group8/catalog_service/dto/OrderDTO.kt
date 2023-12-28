package it.polito.wa2group8.catalog_service.dto

import javax.validation.constraints.Min
import javax.validation.constraints.NotEmpty

data class Product(
    @get:NotEmpty val name: String,
    @get:Min(1) val qty: Int
)

data class OrderDTO(
    @get:NotEmpty val productList: List<Product>,
)
