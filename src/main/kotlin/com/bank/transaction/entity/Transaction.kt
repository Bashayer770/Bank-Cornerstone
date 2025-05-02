package com.bank.transaction.entity

import com.bank.account.entity.Account
import com.bank.currency.entity.Currency
import com.bank.promocode.entity.PromoCode
import jakarta.persistence.*
import java.time.LocalDateTime

@Entity
@Table(name = "transactions")
data class Transaction(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "from_account_id", nullable = false)
    val fromAccount: Account,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "to_account_id", nullable = false)
    val toAccount: Account,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "currency_id", nullable = false)
    val currency: Currency,

    @Column(nullable = false)
    val amount: Double,

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    val status: TransactionStatus,

    @Column(nullable = false)
    val timeStamp: LocalDateTime = LocalDateTime.now(),

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "promo_code_id")
    val promoCode: PromoCode? = null
)

enum class TransactionStatus {
    PENDING,
    COMPLETED,
    FAILED,
    CANCELLED
} 