package com.bank.transaction.repository

import com.bank.transaction.entity.Transaction
import com.bank.transaction.entity.TransactionStatus
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.time.LocalDateTime

@Repository
interface TransactionRepository : JpaRepository<Transaction, Long> {
    fun findByFromAccountId(accountId: Long): List<Transaction>
    fun findByToAccountId(accountId: Long): List<Transaction>
    fun findByStatus(status: TransactionStatus): List<Transaction>
    fun findByTimeStampBetween(start: LocalDateTime, end: LocalDateTime): List<Transaction>
    fun findByPromoCodeId(promoCodeId: Long): List<Transaction>
} 