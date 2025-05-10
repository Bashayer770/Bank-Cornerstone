package com.bank.user

import jakarta.validation.Valid
import jakarta.validation.constraints.NotBlank
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/v1/authentication")
class UsersController(
    private val usersService: UsersService
) {
    @PostMapping("/register")
    fun registerUser(@Valid @RequestBody request: CreateUserDTO): ResponseEntity<Any> {
        return usersService.registerUser(request)
    }
}

data class CreateUserDTO(
    @field:NotBlank(message = "Username is required")
    val username: String,

    @field:NotBlank(message = "Password is required")
    val password: String
)