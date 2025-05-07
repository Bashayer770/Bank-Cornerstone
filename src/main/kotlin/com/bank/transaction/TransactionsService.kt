package com.bank.transaction

import com.bank.account.AccountRepository
import com.bank.account.AccountResponse
import com.bank.currency.CurrencyRepository
import com.bank.exchange.ExchangeRateApi
import com.bank.promocode.PromoCodeRepository
import com.bank.serverMcCache
import com.bank.user.UserRepository
import com.hazelcast.logging.Logger
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Service
import java.math.BigDecimal
import java.math.RoundingMode
import java.time.LocalDateTime
private val  loggerAccount = Logger.getLogger("account")

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

        val requestedCurrency = currencyRepository.findByCountryCode(request.countryCode)
            ?: return ResponseEntity.badRequest().body(mapOf("error" to "Currency not supported"))

        val promoCode = promoCodeRepository.findByCode(103)

        if (account.user.id != userId) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(mapOf("error" to "unauthorized access"))
        }

        if (!account.isActive) {
            return ResponseEntity.badRequest().body(mapOf("error" to "account is not active..."))
        }

        if (request.amount < BigDecimal("1.000") || request.amount > BigDecimal("100000.000")) {
            return ResponseEntity.badRequest().body(mapOf("error" to "amount must be between ${account.currency.countryCode}1 and ${account.currency.countryCode}100,000"))
        }
//        if (account.currency.countryCode != request.countryCode) {
//            return ResponseEntity.badRequest().body(mapOf("error" to "request currency does not match your accounts currency"))
//        }

        val (finalAmount, wasConverted) = if (account.currency.countryCode != request.countryCode) {
            val conversionRate = exchangeRateApi.getRate(request.countryCode, account.currency.countryCode)
            request.amount.multiply(conversionRate).setScale(3, RoundingMode.HALF_UP) to true
        } else {
            request.amount to false
        }

        account.balance += finalAmount
        accountRepository.save(account)

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

        val message = if (wasConverted) {
            "Converted ${requestedCurrency.symbol}${request.amount} to ${account.currency.symbol}${finalAmount} and deposited successfully. new balance: ${account.currency.symbol}${account.balance}"
        } else {
            "${account.currency.symbol}${finalAmount} deposited successfully. new balance: ${account.currency.symbol}${account.balance}"
        }

        val accountCache = serverMcCache.getMap<Long, List<AccountResponse>>("account")
        loggerAccount.info("user=$userId deposited into account=${account.accountNumber}...invalidating cache")
        accountCache.remove(userId)
        return ResponseEntity.ok(mapOf("message" to message))
    }


    fun withdrawAccount(request: DepositRequest, userId: Long?): ResponseEntity<*> {
        val account = accountRepository.findByAccountNumber(request.accountNumber)
            ?: return ResponseEntity.badRequest().body(mapOf("error" to "account with number ${request.accountNumber} was not found"))

        val requestedCurrency = currencyRepository.findByCountryCode(request.countryCode)
            ?: return ResponseEntity.badRequest().body(mapOf("error" to "Currency not supported"))

        val promoCode = promoCodeRepository.findByCode(104)

        if (account.user.id != userId) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(mapOf("error" to "Unauthorized access"))
        }

        if (!account.isActive) {
            return ResponseEntity.badRequest().body(mapOf("error" to "account is not active..."))
        }

//        if (account.currency.countryCode != request.countryCode) {
//            return ResponseEntity.badRequest().body(mapOf("error" to "request currency does not match your accounts currency"))
//        }

        if (request.amount < BigDecimal("1.000") || request.amount > BigDecimal("100000.000")) {
            return ResponseEntity.badRequest().body(mapOf("error" to "amount must be between ${account.currency.countryCode}1 and ${account.currency.countryCode}100,000"))
        }

        if (request.amount > account.balance) {
            return ResponseEntity.badRequest().body(mapOf("error" to "insufficient balance"))
        }

        val (finalAmount, wasConverted) = if (account.currency.countryCode != request.countryCode) {
            val conversionRate = exchangeRateApi.getRate(request.countryCode, account.currency.countryCode)
            request.amount.multiply(conversionRate).setScale(3, RoundingMode.HALF_UP) to true
        } else {
            request.amount to false
        }

        account.balance -= finalAmount
        accountRepository.save(account)

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

        val message = if (wasConverted) {
            "Converted ${requestedCurrency.symbol}${request.amount} to ${account.currency.symbol}${finalAmount} and withdrawn successfully. new balance: ${account.currency.symbol}${account.balance}"
        } else {
            "${account.currency.symbol}${finalAmount} withdrawn successfully. new balance: ${account.currency.symbol}${account.balance}"
        }

        val accountCache = serverMcCache.getMap<Long, List<AccountResponse>>("account")
        loggerAccount.info("user=$userId withdrew from account=${account.accountNumber}...invalidating cache")
        accountCache.remove(userId)

        return ResponseEntity.ok(mapOf("message" to message))
    }
}