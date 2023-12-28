package it.polito.wa2group8.wallet_service.dto

import it.polito.wa2group8.wallet_service.domain.Wallet
import java.math.BigDecimal
import javax.validation.constraints.DecimalMin

data class WalletDTO(
    val walletId: Long?,
    val customerId : Long?,
    @get:DecimalMin(value="0.0", inclusive=true) val currentAmount : BigDecimal?
)

fun Wallet.toWalletDTO() = WalletDTO(getId(), customer.getId(), currentAmount)
