package it.polito.wa2group8.wallet_service.domain

import java.math.BigDecimal
import java.time.LocalDateTime
import javax.persistence.*
import javax.validation.constraints.DecimalMin

enum class TransactionReason
{
    ORDER_PLACEMENT,
    ORDER_RECHARGE,
    OTHER,
}

/**
 * This class maps a transaction to a DBMS
 */
@Entity
class Transaction(
    //The amount of money transacted
    @get:DecimalMin(value="0.0", message="The transaction amount must be greater than 0", inclusive=false)
    @Column(name = "amount", nullable = false)
    val amount: BigDecimal,

    //When the transaction was performed
    @Column(name = "time_instant", nullable = false)
    val timeInstant: LocalDateTime,

    //The wallet which the money was taken from
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "payerWallet", referencedColumnName = "id")
    val payerWallet: Wallet,

    //The wallet which the money was given to
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "beneficiaryWallet", referencedColumnName = "id")
    val beneficiaryWallet: Wallet,

    @Column(name = "reason", nullable = false)
    @Enumerated(EnumType.STRING)
    val reason: TransactionReason,
): EntityBase<Long>()
