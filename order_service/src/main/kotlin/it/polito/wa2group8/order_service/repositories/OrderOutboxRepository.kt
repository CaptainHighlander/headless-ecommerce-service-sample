package it.polito.wa2group8.order_service.repositories

import it.polito.wa2group8.order_service.domain.OrderOutbox
import it.polito.wa2group8.order_service.saga_outbox.events.OrderStatusEvent
import org.springframework.data.jpa.repository.Lock
import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository
import java.util.*
import javax.persistence.LockModeType

@Repository
interface OrderOutboxRepository : CrudRepository<OrderOutbox, Long>
{
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    override fun findById(id: Long): Optional<OrderOutbox>

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    override fun findAll(): MutableIterable<OrderOutbox>

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    override fun <S : OrderOutbox?> save(entity: S): S

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    fun findByOrderId(orderId: Long): OrderOutbox?

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    fun findAllByOrderSagaStatus(orderSagaStatus: OrderStatusEvent): Iterable<OrderOutbox>
}
