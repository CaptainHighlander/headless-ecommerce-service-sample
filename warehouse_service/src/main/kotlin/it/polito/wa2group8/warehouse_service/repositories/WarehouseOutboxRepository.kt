package it.polito.wa2group8.warehouse_service.repositories

import it.polito.wa2group8.warehouse_service.domain.WarehouseOutbox
import org.springframework.data.jpa.repository.Lock
import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository
import java.util.*
import javax.persistence.LockModeType

@Repository
interface WarehouseOutboxRepository : CrudRepository<WarehouseOutbox, Long>
{
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    fun findByOrderId(orderId: Long): WarehouseOutbox?

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    override fun findById(id: Long): Optional<WarehouseOutbox>

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    override fun findAll(): MutableIterable<WarehouseOutbox>

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    override fun <S : WarehouseOutbox?> save(entity: S): S
}
