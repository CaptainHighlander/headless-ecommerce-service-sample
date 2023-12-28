package it.polito.wa2group8.wallet_service.domain

import javax.persistence.*

@Entity
class Customer(
    @Column(nullable = false, unique = true)
    var username: String,
): EntityBase<Long>()
{
    @OneToMany(cascade = [CascadeType.ALL])
    var walletList: MutableSet<Wallet> = mutableSetOf()

    fun addWallet(wallet: Wallet)
    {
        walletList.add(wallet)
    }
}
