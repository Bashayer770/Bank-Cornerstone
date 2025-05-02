package com.bank.account.entity

import com.bank.currency.entity.Currency
import com.bank.transaction.entity.Transaction
import com.bank.user.entity.User
import com.bank.usermembership.entity.UserMembership
import jakarta.persistence.*
import java.time.LocalDateTime

@Entity
@Table(name = "accounts")
data class Account(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    val user: User,

    @Column(nullable = false)
    val balance: Double,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "currency_id", nullable = false)
    val currency: Currency,

    @Column(nullable = false)
    val createdAt: LocalDateTime = LocalDateTime.now(),

    @OneToOne(mappedBy = "account")
    val userMembership: UserMembership? = null,

    @OneToMany(mappedBy = "fromAccount")
    val outgoingTransactions: MutableList<Transaction> = mutableListOf(),

    @OneToMany(mappedBy = "toAccount")
    val incomingTransactions: MutableList<Transaction> = mutableListOf()
) 