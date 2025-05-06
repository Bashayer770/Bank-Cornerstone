package com.bank.kyc

import com.bank.user.UserRepository
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.web.bind.annotation.*
import java.math.BigDecimal
import java.time.LocalDate

@RestController
@RequestMapping("/users/kyc")
class KYCsController(
    private val kycsService: KYCsService,
    private val userRepository: UserRepository
) {

    @GetMapping
    fun getMyKYC(): ResponseEntity<Any> {
        val username = SecurityContextHolder.getContext().authentication.name
        val user = userRepository.findByUsername(username)
            ?: return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build()

        return kycsService.listKYC(user.id!!)
    }

    @PostMapping
    fun addOrUpdateMyKYC(@RequestBody request: KYCRequestDTO): ResponseEntity<Any> {
        val username = SecurityContextHolder.getContext().authentication.name
        val user = userRepository.findByUsername(username)
            ?: return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build()

        return kycsService.addOrUpdateKYC(request, user.id!!)
    }
}


class KYCRequestDTO(
    val firstName: String,
    val lastName: String,
    val dateOfBirth: LocalDate,
    val civilId: String,
    val country: String,
    val phoneNumber: String,
    val homeAddress: String,
    val salary: BigDecimal

)

data class KYCResponseDTO(
    val userId: Long,
    val firstName: String,
    val lastName: String,
    val dateOfBirth: LocalDate,
    val civilId: String,
    val country: String,
    val phoneNumber: String,
    val homeAddress: String,
    val salary: BigDecimal
)