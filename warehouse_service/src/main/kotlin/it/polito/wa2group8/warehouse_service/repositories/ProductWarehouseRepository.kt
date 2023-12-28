package it.polito.wa2group8.warehouse_service.repositories

import it.polito.wa2group8.warehouse_service.domain.Product
import it.polito.wa2group8.warehouse_service.domain.ProductWarehouse
import org.springframework.data.jpa.repository.Lock
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.CrudRepository
import java.util.*
import javax.persistence.LockModeType

interface ProductWarehouseRepository : CrudRepository<ProductWarehouse, Long>
{
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    override fun findById(id: Long): Optional<ProductWarehouse>

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    override fun findAll(): MutableIterable<ProductWarehouse>

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    override fun <S : ProductWarehouse?> save(entity: S): S

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    fun findAllByProduct(product: Product): Iterable<ProductWarehouse>

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT pw FROM ProductWarehouse pw WHERE pw.warehouse.warehouseId = ?1 AND pw.product.productId = ?2")
    fun findByWarehouseIdAndProductId(warehouseId: Long,productId: Long): ProductWarehouse?

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT pw FROM ProductWarehouse pw WHERE pw.product.productId = ?1 AND pw.quantity >= ?2")
    fun findAllByProductIdAndQuantityGreaterThanEqual(productId: Long, quantity: Int): Iterable<ProductWarehouse>

    @Modifying
    @Query("UPDATE ProductWarehouse pw SET pw.quantity = pw.quantity - ?1 WHERE pw.warehouse.warehouseId = ?2 AND pw.product.productId = ?3")
    fun reduceQuantity(quantity: Int, warehouseId: Long, productId: Long) : Int

    @Modifying
    @Query("UPDATE ProductWarehouse pw SET pw.quantity = pw.quantity + ?1 WHERE pw.warehouse.warehouseId = ?2 AND pw.product.productId = ?3")
    fun incrementQuantity(quantity: Int, warehouseId: Long, productId: Long) : Int

    @Modifying
    @Query("DELETE FROM ProductWarehouse pw WHERE pw.warehouse.warehouseId = ?1")
    fun deleteAllByWarehouseId(warehouseId: Long)

    @Modifying
    @Query("DELETE FROM ProductWarehouse pw WHERE pw.product.productId = ?1")
    fun deleteAllByProductId(productId: Long)
}
