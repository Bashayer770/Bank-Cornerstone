package com.bank.account

import com.bank.currency.CurrencyEntity
import com.bank.user.UserEntity
import com.bank.user.UserRepository
import jakarta.persistence.Column
import jakarta.persistence.FetchType
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.web.bind.annotation.*
import java.math.BigDecimal
import java.time.LocalDateTime

@RestController
class AccountsController(
    private val accountsService: AccountsService,
    private val userRepository: UserRepository
) {
    @GetMapping("/api/v1/users/accounts")
    fun listUserAccounts(): ResponseEntity<Any> {
        val username = SecurityContextHolder.getContext().authentication.name
        val user = userRepository.findByUsername(username)
            ?: throw IllegalArgumentException("user has no id...")
        return accountsService.listUserAccounts(user.id)
    }

    @PostMapping("/api/v1/users/accounts")
    fun createAccount(@RequestBody request: CreateAccount): ResponseEntity<Any> {
        val username = SecurityContextHolder.getContext().authentication.name
        val user = userRepository.findByUsername(username)
            ?: throw IllegalArgumentException("user has no id...")
        return accountsService.createAccount(request, user.id)
    }

    @PostMapping("/api/v1/users/accounts/{accountNumber}")
    fun closeAccount(
        @PathVariable accountNumber: String
    ): ResponseEntity<Any> {
        val username = SecurityContextHolder.getContext().authentication.name
        val user = userRepository.findByUsername(username)
            ?: throw IllegalArgumentException("user has no id...")

        return accountsService.closeAccount(accountNumber, user.id)
    }


}

data class CreateAccount(
    val initialBalance: BigDecimal,
    val countryCode: String,
    val accountType: String
)

data class AccountResponse(
    val initialBalance: BigDecimal,
    val accountNumber: String,
    val accountType: String,
    val createdAt: LocalDateTime,
    val countryCode: String,
    val symbol: String
)