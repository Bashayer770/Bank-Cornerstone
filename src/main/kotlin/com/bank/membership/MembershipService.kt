package com.bank.membership

import com.bank.membership.entity.Membership
import com.bank.membership.repository.MembershipRepository
import org.springframework.stereotype.Service

@Service
class MembershipService(
    private val membershipRepository: MembershipRepository
) {
    fun getAll(): List<Membership> = membershipRepository.findAll()

    fun create(membership: Membership): Membership = membershipRepository.save(membership)

    fun getById(id: Long): Membership = membershipRepository.findById(id).orElseThrow()
}