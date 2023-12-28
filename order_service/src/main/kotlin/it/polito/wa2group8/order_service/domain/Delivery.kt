package it.polito.wa2group8.order_service.domain

import javax.persistence.*

//Any order has a delivery list, indicating the shipping address and the warehouse
@Table(name="delivery")
@Entity
class Delivery(
    @Id
    @GeneratedValue(strategy= GenerationType.AUTO)
    @Column(name = "delivery_id", unique = true, nullable = false)
    val deliveryId: Long?,

    //The order associated to this delivery address
    @Column(name = "order_id", nullable = false, unique = true)
    val orderId: Long,

    //The address to which the products belonging to the order will be delivered (Shipping address info)
    @Column(nullable = false)
    val city: String,
    @Column(nullable = false)
    val street: String,
    @Column(nullable = false)
    val zip: String,
)
{
    //The warehouse from which the products will be picked up
    @Column(name="warehouse_id")
    var warehouseId: Long? = null
}
