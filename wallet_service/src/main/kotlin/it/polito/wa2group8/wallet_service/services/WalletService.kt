package it.polito.wa2group8.wallet_service.services

import it.polito.wa2group8.wallet_service.dto.TransactionDTO
import it.polito.wa2group8.wallet_service.dto.WalletDTO

interface WalletService
{
    /**
     * Create a new wallet for a certain customer
     * Return the DTO of the created wallet or null in case of error.
     */
    fun createWallet (token: String) : WalletDTO

    /**
     * Retrieve the list of wallet belonging to the authenticated user who made the request
     */
    fun getWallets(token: String): List<WalletDTO>

    /**
     * Get the details of a wallet given its ID.
     * A nullable WalletDTO objects is returned since a wallet ID might not even exist.
     */
    fun getWalletById(token: String, id: Long) : WalletDTO?

    /**
     * Create a new transaction if it did not already exist
     * Return the DTO of the created transaction or null in case of error.
     */
    fun createTransaction(token: String, payerWalletID: Long, transactionDTO: TransactionDTO) : TransactionDTO?

    /**
     * Get the list of transactions given a walletID between two dates.
     * An empty list will be returned if no transaction meets the requirements.
     */
    fun getTransactionsByWalletId(token: String, walletId: Long, startDate: Long, endDate: Long) : List<TransactionDTO>

    /**
     * Get the details of a transaction given its ID.
     * A nullable TransactionDTO objects is returned since a transaction ID might not even exist.
     */
    fun getTransactionById(token: String, walletId: Long, transactionId: Long) : TransactionDTO?
}