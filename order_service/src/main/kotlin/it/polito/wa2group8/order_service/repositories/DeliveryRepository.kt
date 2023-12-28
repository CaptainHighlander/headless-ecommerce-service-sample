package it.polito.wa2group8.order_service.repositories

import it.polito.wa2group8.order_service.domain.Delivery
import org.springframework.data.jpa.repository.Lock
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository
import java.util.*
import javax.persistence.LockModeType

@Repository
interface DeliveryRepository : CrudRepository<Delivery, Long>
{
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    override fun findById(id: Long): Optional<Delivery>

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    override fun findAll(): MutableIterable<Delivery>

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    fun findByOrderId(orderId: Long): Delivery?

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    override fun <S : Delivery?> save(entity: S): S

    @Modifying
    @Query("UPDATE Delivery d SET d.warehouseId = ?1 WHERE d.orderId = ?2")
    fun updateWarehouseId(warehouseId: Long?, orderId: Long) : Int
}
