package it.polito.wa2group8.warehouse_service.repositories

import it.polito.wa2group8.warehouse_service.domain.Warehouse
import org.springframework.data.jpa.repository.Lock
import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository
import java.util.*
import javax.persistence.LockModeType

@Repository
interface WarehouseRepository : CrudRepository<Warehouse, Long>
{
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    override fun findById(id: Long): Optional<Warehouse>

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    override fun findAll(): MutableIterable<Warehouse>

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    override fun <S : Warehouse?> save(entity: S): S
}
