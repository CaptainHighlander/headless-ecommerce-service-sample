package it.polito.wa2group8.wallet_service.dto

import it.polito.wa2group8.wallet_service.domain.Transaction
import it.polito.wa2group8.wallet_service.domain.TransactionReason
import java.math.BigDecimal
import java.time.ZoneOffset
import javax.validation.constraints.DecimalMin
import javax.validation.constraints.Min
import javax.validation.constraints.Positive

/**
 * This data class models an information offered/consumed by the service
 */
data class TransactionDTO(
    @get:DecimalMin(value="0.0", inclusive=false) val amount: BigDecimal,
    @get:Min(0) val dateInMillis: Long?,
    @get:Positive(message="Invalid payerWalletId") val payerWalletID: Long,
    @get:Positive(message="Invalid beneficiaryWalletId") val beneficiaryWalletID: Long,
    val reason: String,
)


/**
 * An extension function to translate incoming data from the Controller layer to a DTO to be provided to the Service layer
 */
fun Transaction.toTransactionDTO() = TransactionDTO(
    amount,
    timeInstant.toInstant(ZoneOffset.UTC).toEpochMilli(),
    payerWallet.getId() ?: -1,
    beneficiaryWallet.getId() ?: -1,
    reason.toString()
)
