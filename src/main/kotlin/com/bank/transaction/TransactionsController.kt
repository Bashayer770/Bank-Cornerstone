package com.bank.transaction

import com.bank.user.UserRepository
import org.springframework.http.ResponseEntity
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.web.bind.annotation.*
import java.math.BigDecimal
import java.time.LocalDate
import java.util.Locale.IsoCountryCode


@RestController
class TransactionsController(
    private val transactionsService: TransactionsService,
    private val userRepository: UserRepository
) {
    @PostMapping("/api/v1/accounts/deposit")
    fun depositAccount(@RequestBody request: DepositRequest): ResponseEntity<*> {
        val username = SecurityContextHolder.getContext().authentication.name
        val user = userRepository.findByUsername(username) ?: throw IllegalArgumentException("user was not found...")
        return transactionsService.depositAccount(request, user.id)
    }
    @PostMapping("/api/v1/accounts/withdraw")
    fun withdrawAccount(@RequestBody request: WithdrawRequest): ResponseEntity<*> {
        val username = SecurityContextHolder.getContext().authentication.name
        val user = userRepository.findByUsername(username) ?: throw IllegalArgumentException("user was not found...")
        return transactionsService.withdrawAccount(request, user.id)
    }

    @PostMapping("/api/v1/accounts/transfer")
    fun transferAccounts(@RequestBody request: TransferRequest): ResponseEntity<*> {
        val username = SecurityContextHolder.getContext().authentication.name
        val user = userRepository.findByUsername(username) ?: throw IllegalArgumentException("user was not found...")
        return transactionsService.transferAccounts(request, user.id)
    }
}

data class DepositRequest(
    val accountNumber: String,
    val countryCode: String,
    val amount: BigDecimal
)

data class DepositResponse(
    val newBalance: BigDecimal
)

data class WithdrawRequest(
    val accountNumber: String,
    val countryCode: String,
    val amount: BigDecimal
)

data class WithdrawResponse(
    val newBalance: BigDecimal
)

data class TransferRequest(
    val sourceAccount: String,
    val destinationAccount: String,
    val amount: BigDecimal,
    val countryCode: String
)

//data class TransferResponse(
//
//)
