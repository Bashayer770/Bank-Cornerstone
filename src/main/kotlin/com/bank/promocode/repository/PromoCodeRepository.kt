package com.bank.promocode.repository

import com.bank.promocode.entity.PromoCode
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface PromoCodeRepository : JpaRepository<PromoCode, Long> {
    fun findByCode(code: String): PromoCode?
} 