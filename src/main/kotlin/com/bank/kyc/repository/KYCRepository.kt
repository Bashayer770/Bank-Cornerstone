package com.bank.kyc.repository

import com.bank.kyc.entity.KYC
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.UUID

@Repository
interface KYCRepository : JpaRepository<KYC, UUID> {
    fun findByUserId(userId: Long): KYC?
} 