package com.bank.account

import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
class AccountsController(
    private val accountsService: AccountsService
) {
    @GetMapping("/accounts")
    fun listUserAccounts() {

    }
}