package it.polito.wa2group8.wallet_service.domain

import java.math.BigDecimal
import javax.persistence.*
import javax.validation.constraints.DecimalMin

@Entity
class Wallet (
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customerId", referencedColumnName = "id")
    var customer: Customer,

    @Column(name="currentAmount", nullable=false)
    @get:DecimalMin(value="0.0", message="The value must be a positive or zero value", inclusive=true)
    var currentAmount: BigDecimal
): EntityBase<Long>()
{
    @OneToMany(mappedBy = "payerWallet",targetEntity=Transaction::class,fetch = FetchType.LAZY)
    var pList: MutableSet<Transaction> = mutableSetOf() //Purchases list

    @OneToMany(mappedBy = "beneficiaryWallet",targetEntity=Transaction::class,fetch = FetchType.LAZY)
    var rList: MutableSet<Transaction> = mutableSetOf() //Recharges list
}
