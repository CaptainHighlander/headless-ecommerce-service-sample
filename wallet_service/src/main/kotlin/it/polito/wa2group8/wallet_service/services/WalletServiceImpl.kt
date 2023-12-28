package it.polito.wa2group8.wallet_service.services

import it.polito.wa2group8.wallet_service.domain.Customer
import it.polito.wa2group8.wallet_service.domain.Transaction
import it.polito.wa2group8.wallet_service.domain.TransactionReason
import it.polito.wa2group8.wallet_service.domain.Wallet
import it.polito.wa2group8.wallet_service.dto.TransactionDTO
import it.polito.wa2group8.wallet_service.dto.WalletDTO
import it.polito.wa2group8.wallet_service.dto.toTransactionDTO
import it.polito.wa2group8.wallet_service.dto.toWalletDTO
import it.polito.wa2group8.wallet_service.exceptions.BadRequestException
import it.polito.wa2group8.wallet_service.exceptions.ForbiddenException
import it.polito.wa2group8.wallet_service.exceptions.NotFoundException
import it.polito.wa2group8.wallet_service.repositories.CustomerRepository
import it.polito.wa2group8.wallet_service.repositories.TransactionRepository
import it.polito.wa2group8.wallet_service.repositories.WalletRepository
import it.polito.wa2group8.wallet_service.security.JwtUtils
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.math.BigDecimal
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneOffset

@Service
@Transactional
class WalletServiceImpl(
    private val transactionRepo: TransactionRepository,
    private val walletRepo: WalletRepository,
    private val customerRepo: CustomerRepository,
    private val jwtUtils: JwtUtils
) : WalletService
{
    override fun createWallet (token: String) : WalletDTO
    {
        // Get Customer
        val user = jwtUtils.getDetailsFromJwtToken(jwtUtils.getTokenFromHeader(token))
        var customer = customerRepo.findByUsername(user.username)
        if (customer == null)
            customer = customerRepo.save(Customer(user.username))
        //val customer = customerRepo.findByIdOrNull(customerId) ?: throw NotFoundException("Customer not found")

        // Create Wallet entity
        val wallet = Wallet(customer, BigDecimal(0))

        walletRepo.save(wallet)
        customer.addWallet(wallet)
        customerRepo.save(customer)
        return wallet.toWalletDTO()
    }

    override fun getWallets(token: String): List<WalletDTO>
    {
        val user = jwtUtils.getDetailsFromJwtToken(jwtUtils.getTokenFromHeader(token))
        val wallets = walletRepo.findAllByCustomerUsername(user.username)
        return wallets.map { it.toWalletDTO() }
    }

    override fun getWalletById(token: String, id: Long) : WalletDTO?
    {
        val user = jwtUtils.getDetailsFromJwtToken(jwtUtils.getTokenFromHeader(token))
        val wallet = walletRepo.findByIdOrNull(id) ?: throw NotFoundException("Wallet Not Found")
        if (!user.roles.contains("ADMIN"))
        {
            if(wallet.customer.username!=user.username)
                throw ForbiddenException("Forbidden access")
        }
        return wallet.toWalletDTO()
    }

    override fun createTransaction(token: String, payerWalletID: Long, transactionDTO: TransactionDTO) : TransactionDTO?
    {
        val transactionReason = try {
            TransactionReason.valueOf(transactionDTO.reason)
        }
        catch(e: IllegalArgumentException)
        {
            throw BadRequestException("Invalid transaction reason")
        }

        val user = jwtUtils.getDetailsFromJwtToken(jwtUtils.getTokenFromHeader(token))
        // Check if user owns at least one wallet
        val customer = customerRepo.findByUsername(user.username)?:throw NotFoundException("You doesn't own any wallet")
        // Check if payer and beneficiary are different
        if (payerWalletID == transactionDTO.beneficiaryWalletID) throw BadRequestException("Beneficiary and Payer wallet must be different")

        // Get wallets
        val payerWallet = walletRepo.findByIdOrNull(payerWalletID) ?: throw NotFoundException("Payer wallet not found")
        val beneficiaryWallet = walletRepo.findByIdOrNull(transactionDTO.beneficiaryWalletID) ?: throw NotFoundException("Beneficiary wallet not found")
        // Check if the user owns the wallet
        if (payerWallet.customer.username != customer.username) throw ForbiddenException("Forbidden access")
        // Create Transaction entity
        val transaction = Transaction(transactionDTO.amount, LocalDateTime.now(), payerWallet, beneficiaryWallet, transactionReason)


        // Check if currentAmount is sufficient
        if (payerWallet.currentAmount < transaction.amount)
            throw BadRequestException("Payer money not sufficient")

        // Update payer wallet
        payerWallet.currentAmount -= transaction.amount
        walletRepo.save(payerWallet)

        // Update beneficiary wallet
        beneficiaryWallet.currentAmount += transaction.amount
        walletRepo.save(beneficiaryWallet)

        // Save transaction
        return transactionRepo.save(transaction).toTransactionDTO()
    }

    override fun getTransactionsByWalletId(token: String, walletId: Long, startDate: Long, endDate: Long) : List<TransactionDTO>
    {
        val user = jwtUtils.getDetailsFromJwtToken(jwtUtils.getTokenFromHeader(token))
        //Check if endDate > startDate
        if (endDate<startDate) throw  BadRequestException("End date must be after start date")

        // Check if wallet exist
        val wallet = walletRepo.findByIdOrNull(walletId)?: throw NotFoundException("Wallet not found")
        // Check if user own wallet
        if (!user.roles.contains("ADMIN"))
        {
            if(wallet.customer.username != user.username)
                throw ForbiddenException("Forbidden access")
        }

        // Converting date
        val start = Instant.ofEpochMilli(startDate).atZone(ZoneOffset.UTC).toLocalDateTime()
        val end = Instant.ofEpochMilli(endDate).atZone(ZoneOffset.UTC).toLocalDateTime()

        // Get transactions of the specified wallet
        return transactionRepo.findByWalletIdAndTimeInstantBetween(walletId, start, end).map { t -> t.toTransactionDTO() }
    }

    override fun getTransactionById(token: String, walletId: Long, transactionId: Long) : TransactionDTO?
    {
        val user = jwtUtils.getDetailsFromJwtToken(jwtUtils.getTokenFromHeader(token))
        // Check if wallet exist
        val wallet = walletRepo.findByIdOrNull(walletId)?: throw NotFoundException("Wallet not found")
        // Check if user own wallet
        if (!user.roles.contains("ADMIN"))
        {
            if(wallet.customer.username!=user.username)
                throw ForbiddenException("Forbidden access")
        }

        // Get transaction
        val transaction = transactionRepo.findByIdOrNull(transactionId)?.toTransactionDTO()
            ?: throw NotFoundException("Transaction not found")

        // Check if transaction is related to the wallet
        if (walletId != transaction.beneficiaryWalletID && walletId != transaction.payerWalletID)
            throw ForbiddenException("Forbidden access")

        return transaction
    }
}
