package it.polito.wa2group8.order_service.domain

import org.springframework.data.util.ProxyUtils
import javax.persistence.*

enum class OrderStatus
{
    PENDING,        //Order needs to be validated by microservices "Wallet" and "Warehouse"
    ISSUED,         //Order accepted and paid, ready to be delivered
    DELIVERING,     //The items have left the warehouse
    DELIVERED,      //Successful order
    FAILED,         //Order was failed (an order can fail in any moment)
    CANCELED,       //Order was canceled by the user. Cancel requests are considered only if the status is Issued
}

@Table(name = "orders")
@Entity
class Order(
    @Id
    @GeneratedValue(strategy=GenerationType.AUTO)
    @Column(name = "order_id", nullable = false, unique = true)
    var orderId: Long? = null,

    //The id (username) of the buyer who placed the order
    @Column(nullable = false)
    val buyerId: String,

    //The total price of all products that make up the order (also considering their quantity)
    @Column(name = "purchase_price", nullable = false)
    val purchasePrice: Double,
)
{
    //The current order status. PENDING by default
    @Column(name = "status", nullable = false)
    @Enumerated(EnumType.STRING)
    var orderStatus: OrderStatus = OrderStatus.PENDING

    override fun equals(other: Any?): Boolean
    {
        other ?: return false
        if (this === other) return true
        if (javaClass != ProxyUtils.getUserClass(other)) return false

        other as Order
        return if (null == this.orderId) false else this.orderId == other.orderId
    }

    override fun hashCode(): Int = 4321

    fun canBeDeleted(): Boolean
    {
        //An order can be deleted only if status is "Issued"
        return (this.orderStatus == OrderStatus.ISSUED)
    }
}

