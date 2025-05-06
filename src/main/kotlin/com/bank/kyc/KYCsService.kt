package com.bank.kyc

import com.bank.user.UserRepository
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Service
import java.math.BigDecimal
import java.time.LocalDate


@Service
class KYCsService(
    private val kycRepository: KYCRepository,
    private val userRepository: UserRepository
) {
    fun getKYC(userId: Long): ResponseEntity<Any> {
        val kyc = kycRepository.findByUserId(userId)
            ?: return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(mapOf("error" to "User with ID $userId was not found"))

        return ResponseEntity.ok(
            KYCResponseDTO(
                firstName = kyc.firstName,
                lastName = kyc.lastName,
                dateOfBirth = kyc.dateOfBirth,
                civilId = kyc.civilId,
                phoneNumber = kyc.phoneNumber,
                homeAddress = kyc.homeAddress,
                salary = kyc.salary,
                country = kyc.country
            )
        )
    }

    fun addOrUpdateKYC(request: KYCRequestDTO, userId: Long): ResponseEntity<Any> {
        val user = userRepository.findById(userId).orElse(null)
            ?: return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(mapOf("error" to "User with ID $userId was not found"))

        val existing =
            kycRepository.findByUserId(userId) // retrieving whatever data available in the KYC database for this user

        val age = java.time.Period.between(request.dateOfBirth, LocalDate.now()).years
        if (age < 18) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(mapOf("error" to "you must be 18 or older to register"))
        }

        if (request.salary < BigDecimal(100.000) || request.salary > BigDecimal(1000000.000)) {
            return ResponseEntity
                .badRequest()
                .body(mapOf("error" to "salary must be between 100 and 1,000,000 KD"))
        }

        val kyc = if (existing != null) { // updating data
            existing.copy(
                user = user,
                firstName = request.firstName,
                lastName = request.lastName,
                dateOfBirth = request.dateOfBirth,
                salary = request.salary
            )
        } else {
            KYCEntity( // making a new KYC profile for this user
                user = user,
                firstName = request.firstName,
                lastName = request.lastName,
                dateOfBirth = request.dateOfBirth,
                civilId = request.civilId,
                phoneNumber = request.phoneNumber,
                homeAddress = request.homeAddress,
                country = request.country,
                salary = request.salary
            )
        }

        kycRepository.save(kyc) // saving the new/updated data

        return ResponseEntity.ok(user.id?.let {
            KYCResponseDTO( // returning the results of the operation to the client
                firstName = kyc.firstName,
                lastName = kyc.lastName,
                dateOfBirth = kyc.dateOfBirth,
                civilId = kyc.civilId,
                phoneNumber = kyc.phoneNumber,
                homeAddress = kyc.homeAddress,
                salary = kyc.salary,
                country = kyc.country
            )
        })
    }
}