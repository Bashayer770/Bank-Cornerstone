package com.bank.account

import com.bank.user.UserRepository
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.tags.Tag
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import org.springframework.http.ResponseEntity
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.web.bind.annotation.*
import java.math.BigDecimal
import java.time.LocalDateTime

@RestController
@Tag(name = "Account Management", description = "APIs for managing user accounts")
@SecurityRequirement(name = "bearerAuth")
class AccountsController(
    private val accountsService: AccountsService,
    private val userRepository: UserRepository
) {
    @Operation(
        summary = "List user accounts",
        description = "Retrieves all accounts associated with the authenticated user"
    )
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
                description = "Successfully retrieved user accounts",
                content = [Content(mediaType = "application/json", schema = Schema(implementation = ListAccountResponse::class))]
            ),
            ApiResponse(responseCode = "401", description = "Unauthorized - User not authenticated"),
            ApiResponse(responseCode = "404", description = "User not found")
        ]
    )
    @GetMapping("/api/v1/users/accounts")
    fun listUserAccounts(): ResponseEntity<Any> {
        val username = SecurityContextHolder.getContext().authentication.name
        val user = userRepository.findByUsername(username)
            ?: throw IllegalArgumentException("user has no id...")
        return accountsService.listUserAccounts(user.id)
    }

    @Operation(
        summary = "Create new account",
        description = "Creates a new account for the authenticated user with specified currency and type"
    )
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "201",
                description = "Account created successfully",
                content = [Content(mediaType = "application/json", schema = Schema(implementation = CreateAccountResponse::class))]
            ),
            ApiResponse(responseCode = "400", description = "Invalid request parameters"),
            ApiResponse(responseCode = "401", description = "Unauthorized - User not authenticated"),
            ApiResponse(responseCode = "404", description = "User not found")
        ]
    )
    @PostMapping("/api/v1/users/accounts")
    fun createAccount(
        @Parameter(description = "Account creation details", required = true)
        @RequestBody request: CreateAccount
    ): ResponseEntity<Any> {
        val username = SecurityContextHolder.getContext().authentication.name
        val user = userRepository.findByUsername(username)
            ?: throw IllegalArgumentException("user has no id...")
        return accountsService.createAccount(request, user.id)
    }

    @Operation(
        summary = "Close account",
        description = "Closes an existing account for the authenticated user"
    )
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "Account closed successfully"),
            ApiResponse(responseCode = "400", description = "Invalid account number"),
            ApiResponse(responseCode = "401", description = "Unauthorized - User not authenticated"),
            ApiResponse(responseCode = "404", description = "Account not found")
        ]
    )
    @PostMapping("/api/v1/users/accounts/{accountNumber}")
    fun closeAccount(
        @Parameter(description = "Account number to close", required = true)
        @PathVariable accountNumber: String
    ): ResponseEntity<Any> {
        val username = SecurityContextHolder.getContext().authentication.name
        val user = userRepository.findByUsername(username)
            ?: throw IllegalArgumentException("user has no id...")

        return accountsService.closeAccount(accountNumber, user.id)
    }
}

@Schema(description = "Request object for creating a new account")
data class CreateAccount(
    @Schema(description = "Initial balance for the account", example = "1000.00")
    val initialBalance: BigDecimal,
    
    @Schema(description = "Country code for the account currency", example = "USD")
    val countryCode: String,
    
    @Schema(description = "Type of account", example = "SAVINGS")
    val accountType: String
)

@Schema(description = "Response object for account listing")
data class ListAccountResponse(
    @Schema(description = "Current balance of the account", example = "1000.00")
    val balance: BigDecimal,
    
    @Schema(description = "Unique account number", example = "1234567890")
    val accountNumber: String,
    
    @Schema(description = "Type of account", example = "SAVINGS")
    val accountType: String,
    
    @Schema(description = "Account creation timestamp")
    val createdAt: LocalDateTime,
    
    @Schema(description = "Country code for the account currency", example = "USD")
    val countryCode: String,
    
    @Schema(description = "Currency symbol", example = "$")
    val symbol: String,
    
    @Schema(description = "Account tier level", example = "SILVER")
    val accountTier: String,
    
    @Schema(description = "Loyalty points", example = "100")
    val points: Int
)

@Schema(description = "Response object for account creation")
data class CreateAccountResponse(
    @Schema(description = "Initial balance of the account", example = "1000.00")
    val balance: BigDecimal,
    
    @Schema(description = "Unique account number", example = "1234567890")
    val accountNumber: String,
    
    @Schema(description = "Type of account", example = "SAVINGS")
    val accountType: String,
    
    @Schema(description = "Account creation timestamp")
    val createdAt: LocalDateTime,
    
    @Schema(description = "Country code for the account currency", example = "USD")
    val countryCode: String,
    
    @Schema(description = "Currency symbol", example = "$")
    val symbol: String,
)