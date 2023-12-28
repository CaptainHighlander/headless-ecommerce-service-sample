package it.polito.wa2group8.order_service.repositories

import it.polito.wa2group8.order_service.domain.Order
import it.polito.wa2group8.order_service.domain.OrderStatus
import org.springframework.data.jpa.repository.Lock
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository
import java.util.*
import javax.persistence.LockModeType

@Repository
interface OrderRepository : CrudRepository<Order, Long>
{
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    override fun findById(id: Long): Optional<Order>

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    override fun findAll(): MutableIterable<Order>

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    override fun <S : Order?> save(entity: S): S

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    fun findAllByBuyerId(buyerId: String): Iterable<Order>

    @Modifying
    @Query("UPDATE Order o SET o.orderStatus = ?1 WHERE o.orderId = ?2")
    fun updateStatus(status: OrderStatus, orderId: Long)
}