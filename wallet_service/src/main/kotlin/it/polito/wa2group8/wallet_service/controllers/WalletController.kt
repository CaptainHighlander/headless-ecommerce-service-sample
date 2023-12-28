package it.polito.wa2group8.wallet_service.controllers

import it.polito.wa2group8.wallet_service.dto.TransactionDTO
import it.polito.wa2group8.wallet_service.services.WalletService
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.validation.BindingResult
import org.springframework.web.bind.annotation.*
import javax.validation.Valid

@RestController
class WalletController(val walletService: WalletService)
{
    @PostMapping(value = ["/wallets"], produces=[MediaType.APPLICATION_JSON_VALUE])
    @ResponseBody
    fun createWallet(@RequestHeader("Authorization") token: String): ResponseEntity<Any>
    {
        return ResponseEntity.status(HttpStatus.CREATED).body(walletService.createWallet(token))
    }

    @GetMapping(value = ["/wallets/"], produces=[MediaType.APPLICATION_JSON_VALUE])
    @ResponseBody
    fun getWallets(@RequestHeader("Authorization") token: String): ResponseEntity<Any>
    {
        return ResponseEntity.ok().body(walletService.getWallets(token))
    }

    @GetMapping(value = ["/wallets/{walletId}"], produces=[MediaType.APPLICATION_JSON_VALUE])
    @ResponseBody
    fun getWalletById(@RequestHeader("Authorization") token: String, @PathVariable("walletId") walletId: Long): ResponseEntity<Any>
    {
        return ResponseEntity.ok().body(walletService.getWalletById(token, walletId))
    }

    @PostMapping(value = ["/wallets/{walletId}/transactions"], produces = [MediaType.APPLICATION_JSON_VALUE])
    @ResponseBody
    fun createTransaction(
        @RequestHeader("Authorization") token: String,
        @PathVariable("walletId") payer_wallet_id: Long,
        @RequestBody @Valid transaction: TransactionDTO,
        bindingResult: BindingResult
    ): ResponseEntity<Any>
    {
        if (bindingResult.hasErrors())
            return ResponseEntity.badRequest().body(bindingResult.fieldErrors)
        return ResponseEntity.status(HttpStatus.CREATED).body(walletService.createTransaction(token, payer_wallet_id, transaction))
    }

    @GetMapping(value = ["/wallets/{id}/transactions"], produces = [MediaType.APPLICATION_JSON_VALUE])
    @ResponseBody
    fun getWalletTransactionsById(
        @RequestHeader("Authorization") token: String,
        @PathVariable("id") wallet_id: Long,
        @RequestParam("from", required= true) from: Long,
        @RequestParam("to",required = true) to: Long
    ): ResponseEntity<Any>
    {
        return ResponseEntity.ok().body(walletService.getTransactionsByWalletId(token, wallet_id,from,to))
    }

    @GetMapping(value = ["/wallets/{walletId}/transactions/{transactionId}"], produces = [MediaType.APPLICATION_JSON_VALUE])
    @ResponseBody
    fun getTransactionById(
        @RequestHeader("Authorization") token: String,
        @PathVariable("walletId") walletId: Long,
        @PathVariable("transactionId") transactionId: Long
    ): ResponseEntity<Any>
    {
        return ResponseEntity.ok().body(walletService.getTransactionById(token, walletId, transactionId))
    }
}
