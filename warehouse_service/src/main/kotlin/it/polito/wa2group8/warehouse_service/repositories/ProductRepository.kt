package it.polito.wa2group8.warehouse_service.repositories

import it.polito.wa2group8.warehouse_service.domain.Product
import org.springframework.data.jpa.repository.Lock
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository
import java.math.BigDecimal
import java.util.*
import javax.persistence.LockModeType

@Repository
interface ProductRepository : CrudRepository<Product, Long>
{
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    override fun findById(id: Long): Optional<Product>

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    override fun findAll(): MutableIterable<Product>

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    override fun <S : Product?> save(entity: S): S

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    fun findByProductIdAndPrice(productId: Long, price: BigDecimal): Product?

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    fun findByCategory(category: String): Iterable<Product>

    @Modifying
    @Query("UPDATE Product p SET p.commentsNumber = ?1, p.averageRating = ?2 WHERE p.productId = ?3")
    fun updateCommentsNoAndAverageRating(commentsNumber: Int, averageRating: BigDecimal, productId: Long): Int
}
