package com.bank.membership

import com.bank.membership.entity.Membership
import com.bank.membership.entity.MembershipTier
import com.bank.membership.repository.MembershipRepository
import org.springframework.stereotype.Service

@Service
class MembershipService(
    private val membershipRepository: MembershipRepository
) {
    fun getAll(): List<Membership> = membershipRepository.findAll()

    fun getById(id: Long): Membership = membershipRepository.findById(id)
        .orElseThrow { NoSuchElementException("Membership with ID $id not found") }

    fun getByTierName(name: String): Membership? {
        return try {
            val tier = MembershipTier.valueOf(name.uppercase())
            membershipRepository.findByName(tier)
        } catch (e: IllegalArgumentException) {
            null
        }
    }
}