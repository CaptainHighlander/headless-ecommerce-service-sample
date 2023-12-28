package it.polito.wa2group8.order_service.domain

import it.polito.wa2group8.order_service.dto.PurchasedProductInput
import javax.persistence.*

@Table(name = "purchased_product")
@Entity
class PurchasedProduct(
    @Id
    @GeneratedValue(strategy= GenerationType.AUTO)
    @Column(name = "purchased_product_id")
    val purchasedProductID: Long? = null,

    //The id of the product (i.e. the id inside microservice warehouse)
    @Column(nullable = false)
    var productId: Long,

    //The name of the product
    @Column(nullable = false)
    var name: String,

    //The price of the product
    @Column(nullable = false)
    var price: Double,

    //The number of wanted product.
    @Column(nullable = false)
    var quantity: Int,

    //The order to which the product is associated
    @Column(name = "order_id", nullable = false)
    var orderId: Long,
)

fun PurchasedProductInput.toPurchaseProduct(orderId: Long) = PurchasedProduct(null,
    productId,
    name,
    price,
    quantity,
    orderId,
)