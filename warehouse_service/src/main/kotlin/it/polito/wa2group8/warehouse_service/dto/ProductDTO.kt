package it.polito.wa2group8.warehouse_service.dto

import org.springframework.format.annotation.DateTimeFormat
import java.math.BigDecimal
import java.util.*
import javax.validation.constraints.DecimalMin

data class ProductDTO (
    val id: Long?,

    val name: String?,

    val description: String?,

    val category: String?,

    @get:DecimalMin(value = "0.0", message="Negative price", inclusive = true)
    val price: BigDecimal?,

    @get:DecimalMin(value = "0.0", message="The value must be a positive or zero value", inclusive = true)
    var averageRating: BigDecimal?,

    @get:DateTimeFormat
    var creationDate: Date?
)
