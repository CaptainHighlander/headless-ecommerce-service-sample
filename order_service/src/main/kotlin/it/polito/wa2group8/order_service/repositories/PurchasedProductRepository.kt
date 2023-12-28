package it.polito.wa2group8.order_service.repositories

import it.polito.wa2group8.order_service.domain.PurchasedProduct
import org.springframework.data.jpa.repository.Lock
import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository
import java.util.*
import javax.persistence.LockModeType

@Repository
interface PurchasedProductRepository : CrudRepository<PurchasedProduct, Long>
{
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    fun findAllByOrderId(orderId: Long): Iterable<PurchasedProduct>

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    override fun findById(id: Long): Optional<PurchasedProduct>

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    override fun findAll(): MutableIterable<PurchasedProduct>

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    override fun <S : PurchasedProduct?> save(entity: S): S
}
