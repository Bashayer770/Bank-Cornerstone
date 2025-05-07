package com.bank.transaction

import com.bank.account.AccountRepository
import com.bank.currency.CurrencyRepository
import com.bank.exchange.ExchangeRateApi
import com.bank.promocode.PromoCodeRepository
import com.bank.user.UserRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Service
import java.math.BigDecimal
import java.math.RoundingMode
import java.time.LocalDateTime

@Service
class TransactionsService(
    private val userRepository: UserRepository,
    private val accountRepository: AccountRepository,
    private val transactionRepository: TransactionRepository,
    private val promoCodeRepository: PromoCodeRepository,
    private val currencyRepository: CurrencyRepository,
) {
    @Autowired
    private lateinit var exchangeRateApi: ExchangeRateApi

    fun depositAccount(request: DepositRequest, userId: Long?): ResponseEntity<*> {
        val account = accountRepository.findByAccountNumber(request.accountNumber)
            ?: return ResponseEntity.badRequest().body(mapOf("error" to "account with number ${request.accountNumber} was not found"))

        if (account.user.id != userId) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(mapOf("error" to "unauthorized access"))
        }

        if (!account.isActive) {
            return ResponseEntity.badRequest().body(mapOf("error" to "account is not active..."))
        }

        if (account.currency.countryCode != request.countryCode) {
            return ResponseEntity.badRequest().body(mapOf("error" to "request currency does not match your accounts currency"))
        }

        if (request.amount < BigDecimal("1.000") || request.amount > BigDecimal("100000.000")) {
            return ResponseEntity.badRequest().body(mapOf("error" to "Amount must be between ${account.currency.countryCode}1 and ${account.currency.countryCode}100,000"))
        }

        account.balance += request.amount
        accountRepository.save(account)

        val promoCode = promoCodeRepository.findByCode(103)

        transactionRepository.save(TransactionEntity(
            sourceAccount = null,
            destinationAccount = account,
            currency = account.currency,
            amount = request.amount,
            timeStamp = LocalDateTime.now(),
            promoCode = promoCode,
            status = TransactionStatus.COMPLETED,
            transactionType = TransactionType.DEPOSIT

        ))

        return ResponseEntity.ok(mapOf("message" to "${account.currency.symbol}${request.amount} has been deposited successfully into your account, new balance: ${account.currency.symbol}${account.balance}"))
    }

    fun withdrawAccount(request: DepositRequest, userId: Long?): ResponseEntity<*> {
        val account = accountRepository.findByAccountNumber(request.accountNumber)
            ?: return ResponseEntity.badRequest().body(mapOf("error" to "account with number ${request.accountNumber} was not found"))

        if (account.user.id != userId) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(mapOf("error" to "Unauthorized access"))
        }

        if (!account.isActive) {
            return ResponseEntity.badRequest().body(mapOf("error" to "account is not active..."))
        }

        if (account.currency.countryCode != request.countryCode) {
            return ResponseEntity.badRequest().body(mapOf("error" to "request currency does not match your accounts currency"))
        }

        if (request.amount < BigDecimal("1.000") || request.amount > BigDecimal("100000.000")) {
            return ResponseEntity.badRequest().body(mapOf("error" to "amount must be between ${account.currency.countryCode}1 and ${account.currency.countryCode}100,000"))
        }

        if (request.amount > account.balance) {
            return ResponseEntity.badRequest().body(mapOf("error" to "insufficient balance"))
        }

        account.balance -= request.amount
        accountRepository.save(account)

        val promoCode = promoCodeRepository.findByCode(104)

        transactionRepository.save(TransactionEntity(
            sourceAccount = account,
            destinationAccount = null,
            currency = account.currency,
            amount = request.amount,
            timeStamp = LocalDateTime.now(),
            promoCode = promoCode,
            status = TransactionStatus.COMPLETED,
            transactionType = TransactionType.WITHDRAWAL

        ))

        return ResponseEntity.ok(mapOf("message" to "${account.currency.symbol}${request.amount} has been withdrew successfully from your account, new balance: ${account.currency.symbol}${account.balance}"))
    }
}