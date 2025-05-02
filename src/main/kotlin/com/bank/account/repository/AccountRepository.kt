package com.bank.account.repository

import com.bank.account.entity.Account
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface AccountRepository : JpaRepository<Account, Long> {
    fun findByUserId(userId: Long): List<Account>
    fun findByCurrencyId(currencyId: Long): List<Account>
} 