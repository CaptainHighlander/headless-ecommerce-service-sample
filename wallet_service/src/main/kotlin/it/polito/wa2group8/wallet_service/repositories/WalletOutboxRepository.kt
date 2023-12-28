package it.polito.wa2group8.wallet_service.repositories

import it.polito.wa2group8.wallet_service.domain.WalletOutbox
import org.springframework.data.jpa.repository.Lock
import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository
import java.util.*
import javax.persistence.LockModeType

@Repository
interface WalletOutboxRepository : CrudRepository<WalletOutbox, Long>
{
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    fun findByOrderId(orderId: Long): WalletOutbox?

    override fun findById(id: Long): Optional<WalletOutbox>

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    override fun findAll(): MutableIterable<WalletOutbox>

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    override fun <S : WalletOutbox?> save(entity: S): S
}
