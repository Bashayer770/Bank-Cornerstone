package com.bank.membership.repository

import com.bank.membership.entity.Membership
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface MembershipRepository : JpaRepository<Membership, Long> {
    fun findByName(name: String): Membership?
} 