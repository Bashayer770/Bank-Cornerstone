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
            ?: return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build()
        return accountsService.listUserAccounts(user.id)
    }

    @PostMapping("/api/v1/users/accounts")
    fun createAccount(@RequestBody request: CreateAccountDTO): ResponseEntity<Any> {
        val username = SecurityContextHolder.getContext().authentication.name
        val user = userRepository.findByUsername(username)
            ?: return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build()
        return accountsService.createAccount(request, user.id)
    }

    @PostMapping("/api/v1/users/accounts/{accountNumber}")
    fun closeAccount(
        @PathVariable accountNumber: String
    ): ResponseEntity<Any> {
        val username = SecurityContextHolder.getContext().authentication.name
        val user = userRepository.findByUsername(username)
            ?: return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build()

        return accountsService.closeAccount(accountNumber, user.id)
    }


}

data class CreateAccountDTO(
    val initialBalance: BigDecimal,
    val currencyCode: String,
    val accountType: String
)

data class AccountResponseDTO(
    val initialBalance: BigDecimal,
    val accountNumber: String,
    val accountType: String,
    val createdAt: LocalDateTime,
    val currencyCode: String,
)