package com.bank.account

import com.bank.currency.CurrencyRepository
import com.bank.user.UserRepository
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Service
import java.math.BigDecimal
import java.security.SecureRandom
import java.time.LocalDateTime


@Service
class AccountsService(
    private val accountRepository: AccountRepository,
    private val userRepository: UserRepository,
    private val currencyRepository: CurrencyRepository
) {
    fun listUserAccounts(userId: Long?): ResponseEntity<Any> {
        val accounts = accountRepository.findByUserId(userId).filter { it.isActive }
        if (accounts.isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(mapOf("error" to "no accounts found for user ID $userId"))
        }

        return ResponseEntity.ok().body(
            accounts.map {
                AccountResponseDTO(
                    initialBalance = it.balance,
                    accountNumber = it.accountNumber,
                    accountType = it.accountType,
                    createdAt = it.createdAt,
                    currencyCode = it.currency.countryCode,
                    symbol = it.currency.symbol

                )
            }
        )
    }

    fun createAccount(request: CreateAccountDTO, userId: Long?): ResponseEntity<Any> {
        val currency = currencyRepository.findByCountryCode(request.currencyCode)
            ?: return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(mapOf("error" to "Invalid currency code: ${request.currencyCode}"))

        val user = userRepository.findById(userId)
            ?: return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(mapOf("error" to "user with ID $userId was not found"))

        if (request.initialBalance < BigDecimal(0.000) || request.initialBalance > BigDecimal(1000000.000)) {
            return ResponseEntity
                .badRequest()
                .body(mapOf("error" to "Initial balance must be between 0 and 1,000,000 ${currency.countryCode}"))
        }

        val userAccounts = accountRepository.findAll().filter { it.user.id == user.id && it.isActive }
        if (userAccounts.size >= 5) {
            return ResponseEntity
                .badRequest()
                .body(mapOf("error" to "user has reached the maximum limit of 5 active accounts"))
        }

        val account = accountRepository.save(AccountEntity(
            user = user,
            balance = request.initialBalance,
            currency = currency,
            createdAt = LocalDateTime.now(),
            isActive = true,
            accountNumber = generateUniqueAccountNumber(),
            accountType = request.accountType
        ))

        return ResponseEntity.ok().body(AccountResponseDTO(
            initialBalance = account.balance,
            accountNumber = account.accountNumber,
            accountType = account.accountType,
            createdAt = account.createdAt,
            currencyCode = account.currency.countryCode,
            symbol = account.currency.symbol
        ))
    }

    fun closeAccount(accountNumber: String, userId: Long?): ResponseEntity<Any> {
        val account = accountRepository.findAll()
            .firstOrNull { it.accountNumber == accountNumber && it.user.id == userId }
            ?: return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(mapOf("error" to "account not found or does not belong to the user"))

        if (!account.isActive) {
            return ResponseEntity.badRequest()
                .body(mapOf("error" to "account is already closed"))
        }

        if (account.balance > BigDecimal.ZERO) {
            return ResponseEntity.badRequest()
                .body(mapOf("error" to "cannot close account with non-zero balance"))
        }

        val closedAccount = account.copy(isActive = false)
        accountRepository.save(closedAccount)

        return ResponseEntity.ok(mapOf("message" to "account closed successfully"))
    }


    fun generateSecureAccountNumber(): String {
        val secureRandom = SecureRandom()
        val prefix = "77"
        val randomDigits = (1..12)
            .map { secureRandom.nextInt(10) }
            .joinToString("")
        return "$prefix$randomDigits"
    }
    fun generateUniqueAccountNumber(): String {
        var accountNumber: String
        do {
            accountNumber = generateSecureAccountNumber()
        } while (accountRepository.existsByAccountNumber(accountNumber))
        return accountNumber
    }
}