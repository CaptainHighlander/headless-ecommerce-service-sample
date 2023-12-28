package it.polito.wa2group8.wallet_service.repositories

import it.polito.wa2group8.wallet_service.domain.Customer
import it.polito.wa2group8.wallet_service.domain.Wallet
import org.springframework.data.jpa.repository.Lock
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository
import java.math.BigDecimal
import java.util.*
import javax.persistence.LockModeType

@Repository
interface WalletRepository : CrudRepository<Wallet,Long>
{
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    fun findFirstByCustomerAndCurrentAmountGreaterThanEqual(customer: Customer, currentAmount: BigDecimal): Wallet?

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT w FROM Wallet w WHERE w.customer.username = ?1")
    fun findFirstByCustomerUsername(username: String): Wallet?

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT w FROM Wallet w WHERE w.customer.username = ?1")
    fun findAllByCustomerUsername(username: String): MutableIterable<Wallet>

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    override fun findById(id: Long): Optional<Wallet>

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    override fun findAll(): MutableIterable<Wallet>

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    override fun <S : Wallet?> save(entity: S): S

    @Modifying
    @Query("UPDATE Wallet w SET w.currentAmount = ?1 WHERE w.id = ?2")
    fun updateAmount(currentAmount: BigDecimal, walletId: Long)
}
