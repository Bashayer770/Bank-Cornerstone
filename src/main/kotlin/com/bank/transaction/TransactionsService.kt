package com.bank.transaction

import com.bank.account.AccountRepository
import com.bank.account.AccountResponse
import com.bank.currency.CurrencyRepository
import com.bank.exchange.ExchangeRateApi
import com.bank.promocode.PromoCodeRepository
import com.bank.serverMcCache
import com.hazelcast.logging.Logger
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Service
import java.lang.Thread.sleep
import java.math.BigDecimal
import java.math.RoundingMode
import java.time.LocalDateTime
private val loggerAccount = Logger.getLogger("account")
const val TRANSACTION_TYPE_DEPOSIT = 103
const val TRANSACTION_TYPE_WITHDRAW = 104
const val TRANSACTION_TYPE_TRANSFER = 101
const val TRANSACTION_TYPE_FEE = 102


@Service
class TransactionsService(
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

        val promoCodeDeposit = promoCodeRepository.findByCode(TRANSACTION_TYPE_DEPOSIT)
        val promoCodeFee = promoCodeRepository.findByCode(TRANSACTION_TYPE_FEE)


        if (account.user.id != userId) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(mapOf("error" to "unauthorized access"))
        }

        if (!account.isActive) {
            return ResponseEntity.badRequest().body(mapOf("error" to "account is not active..."))
        }

        if (request.amount < BigDecimal("1.000") || request.amount > BigDecimal("100000.000")) {
            return ResponseEntity.badRequest().body(mapOf("error" to "amount must be between ${account.currency.symbol}1 and ${account.currency.symbol}100,000"))
        }

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
            currency = requestedCurrency,
            amount = request.amount,
            timeStamp = LocalDateTime.now(),
            promoCode = promoCodeDeposit,
            status = TransactionStatus.COMPLETED
        ))

        val accountCache = serverMcCache.getMap<Long, List<AccountResponse>>("account")
        loggerAccount.info("user=$userId deposited into account=${account.accountNumber}...invalidating cache")
        accountCache.remove(userId)

        return ResponseEntity.ok().body(DepositResponse(
            newBalance = account.balance,
            transferStatus = TransactionStatus.COMPLETED.toString(),
            isConverted = wasConverted,
            amountDeposited = finalAmount
        ))
    }


    fun withdrawAccount(request: WithdrawRequest, userId: Long?): ResponseEntity<*> {
        val account = accountRepository.findByAccountNumber(request.accountNumber)
            ?: return ResponseEntity.badRequest().body(mapOf("error" to "account with number ${request.accountNumber} was not found"))

        val requestedCurrency = currencyRepository.findByCountryCode(request.countryCode)
            ?: return ResponseEntity.badRequest().body(mapOf("error" to "Currency not supported"))

        val promoCode = promoCodeRepository.findByCode(TRANSACTION_TYPE_WITHDRAW)

        if (account.user.id != userId) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(mapOf("error" to "Unauthorized access"))
        }

        if (!account.isActive) {
            return ResponseEntity.badRequest().body(mapOf("error" to "account is not active..."))
        }

        if (request.amount < BigDecimal("1.000") || request.amount > BigDecimal("100000.000")) {
            return ResponseEntity.badRequest().body(mapOf("error" to "amount must be between ${account.currency.symbol}1 and ${account.currency.symbol}100,000"))
        }

        val (finalAmount, wasConverted) = if (account.currency.countryCode != request.countryCode) {
            val conversionRate = exchangeRateApi.getRate(request.countryCode, account.currency.countryCode)
            request.amount.multiply(conversionRate).setScale(3, RoundingMode.HALF_UP) to true
        } else {
            request.amount to false
        }

        if (finalAmount > account.balance) {
            return ResponseEntity.badRequest().body(mapOf("error" to "insufficient balance"))
        }

        account.balance -= finalAmount
        accountRepository.save(account)

        transactionRepository.save(TransactionEntity(
            sourceAccount = account,
            destinationAccount = null,
            currency = requestedCurrency,
            amount = request.amount,
            timeStamp = LocalDateTime.now(),
            promoCode = promoCode,
            status = TransactionStatus.COMPLETED

        ))

        val accountCache = serverMcCache.getMap<Long, List<AccountResponse>>("account")
        loggerAccount.info("user=$userId withdrew from account=${account.accountNumber}...invalidating cache")
        accountCache.remove(userId)

        return ResponseEntity.ok().body(WithdrawResponse(
            newBalance = account.balance,
            transferStatus = TransactionStatus.COMPLETED.toString(),
            isConverted = wasConverted,
            amountWithdrawn = finalAmount
        ))
    }

    fun transferAccounts(request: TransferRequest, userId: Long?): ResponseEntity<*> {
        println("in transfer function service")
        val sourceAccount = accountRepository.findByAccountNumber(request.sourceAccount)
            ?: return ResponseEntity.badRequest().body(mapOf("error" to "account with number ${request.sourceAccount} was not found"))

        val destinationAccount = accountRepository.findByAccountNumber(request.destinationAccount)
            ?: return ResponseEntity.badRequest().body(mapOf("error" to "account with number ${request.destinationAccount} was not found"))

        val requestedCurrency = currencyRepository.findByCountryCode(request.countryCode)
            ?: return ResponseEntity.badRequest().body(mapOf("error" to "unsupported currency"))

        val promoCode = promoCodeRepository.findByCode(TRANSACTION_TYPE_TRANSFER)

        val feePromo = promoCodeRepository.findByCode(TRANSACTION_TYPE_FEE)

        if (sourceAccount.user.id != userId) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(mapOf("error" to "Unauthorized access"))
        }

        if (destinationAccount.user.id != userId) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(mapOf("error" to "Unauthorized access"))
        }

        if (!sourceAccount.isActive) {
            return ResponseEntity.badRequest().body(mapOf("error" to "source account is not active..."))
        }

        if (!destinationAccount.isActive) {
            return ResponseEntity.badRequest().body(mapOf("error" to "destination account is not active..."))
        }

        if (request.amount < BigDecimal("1.000") || request.amount > BigDecimal("100000.000")) {
            return ResponseEntity.badRequest().body(mapOf("error" to "amount must be between ${requestedCurrency.symbol}1 and ${requestedCurrency.symbol}100,000"))
        }

        if (request.sourceAccount == request.destinationAccount) {
            return ResponseEntity.badRequest().body(mapOf("error" to "you cannot transfer funds between the same account..."))
        }

        val (fromRate, wasSourceConverted) = if (request.countryCode != sourceAccount.currency.countryCode){exchangeRateApi.getRate(request.countryCode, sourceAccount.currency.countryCode) to true}
        else{BigDecimal("1.000") to false}
        val sourceAmount = request.amount.multiply(fromRate).setScale(3, RoundingMode.HALF_UP)

        val (toRate, wasDestinationConverted) = if(request.countryCode != destinationAccount.currency.countryCode) {exchangeRateApi.getRate(request.countryCode, destinationAccount.currency.countryCode) to true}
        else{BigDecimal("1.000") to false}
        val destinationAmount = request.amount.multiply(toRate).setScale(3, RoundingMode.HALF_UP)

        val feeRate = if("USD" != sourceAccount.currency.countryCode){exchangeRateApi.getRate("USD", sourceAccount.currency.countryCode)}
        else{BigDecimal("1.000")}
        val feeInAccountCurrency =
            feeRate.multiply(BigDecimal("15.000"))
            .setScale(3, RoundingMode.HALF_UP)

        if (sourceAmount > sourceAccount.balance) {
            return ResponseEntity.badRequest().body(mapOf("error" to "insufficient balance"))
        }

        sourceAccount.balance -= sourceAmount

        if (feeInAccountCurrency > sourceAccount.balance) {
            return ResponseEntity.badRequest().body(mapOf("error" to "insufficient balance"))
        }

        sourceAccount.balance -= feeInAccountCurrency
        destinationAccount.balance += destinationAmount


        accountRepository.save(sourceAccount)
        accountRepository.save(destinationAccount)

        transactionRepository.save(TransactionEntity(
            sourceAccount = sourceAccount,
            destinationAccount = destinationAccount,
            currency = requestedCurrency,
            amount = request.amount,
            timeStamp = LocalDateTime.now(),
            promoCode = promoCode,
            status = TransactionStatus.COMPLETED
        ))

        transactionRepository.save(TransactionEntity(
            sourceAccount = sourceAccount,
            destinationAccount = null,
            currency = sourceAccount.currency,
            amount = feeInAccountCurrency,
            timeStamp = LocalDateTime.now(),
            promoCode = feePromo,
            status = TransactionStatus.COMPLETED
        ))

        val accountCache = serverMcCache.getMap<Long, List<AccountResponse>>("account")
        loggerAccount.info("transfer occurred...invalidating cache")
        accountCache.remove(userId)

        return ResponseEntity.ok().body(TransferResponse(
            sourceNewBalance = sourceAccount.balance,
            transferStatus = TransactionStatus.COMPLETED.toString(),
            isSourceConverted = wasSourceConverted,
            sourceAmountWithdrawn = sourceAmount,
            transferFee = feeInAccountCurrency
        ))
    }
}