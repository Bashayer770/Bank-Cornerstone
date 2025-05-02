package com.bank.usermembership.repository

import com.bank.usermembership.entity.UserMembership
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.time.LocalDateTime

@Repository
interface UserMembershipRepository : JpaRepository<UserMembership, Long> {
    fun findByUserId(userId: Long): List<UserMembership>
    fun findByAccountId(accountId: Long): UserMembership?
    fun findByMembershipTierId(membershipTierId: Long): List<UserMembership>
    fun findByStartedAtBetween(start: LocalDateTime, end: LocalDateTime): List<UserMembership>
    fun findByEndedAtIsNull(): List<UserMembership>
} 