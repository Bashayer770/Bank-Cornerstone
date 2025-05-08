package com.bank.membership

import com.bank.user.UserRepository
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.web.bind.annotation.*


@RestController
class MembershipController(
    private val membershipService: MembershipService,
    private val userRepository: UserRepository
) {
    @GetMapping("/api/v1/memberships")
    fun getAll(): List<MembershipTierEntity> {
        val username = SecurityContextHolder.getContext().authentication.name
        val user = userRepository.findByUsername(username) ?: throw IllegalArgumentException("user has no id...")
        return membershipService.getAll(user.id)
    }

    @GetMapping("/api/v1/memberships/tier/{name}")
    fun getByTierName(@PathVariable name: String): MembershipTierEntity? {
        return membershipService.getByTierName(name)
    }
}