package it.polito.wa2group8.warehouse_service.domain

import javax.persistence.*
import javax.validation.constraints.Min

@Entity
class ProductWarehouse (
    @Id
    @GeneratedValue(strategy=GenerationType.AUTO)
    var id: Long? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", referencedColumnName = "product_id")
    val product: Product,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "warehouse_id", referencedColumnName = "warehouse_id")
    val warehouse: Warehouse,

    @get:Min(value = 0, message = "Quantity cannot be negative")
    var quantity: Int = 0,

    @get:Min(value = 1, message = "Alarm on quantity cannot be lower than 1")
    val alarmOnQuantity: Int = 1,
)
