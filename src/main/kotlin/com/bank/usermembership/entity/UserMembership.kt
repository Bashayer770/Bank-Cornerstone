package com.bank.usermembership.entity

import com.bank.account.entity.Account
import com.bank.membership.entity.Membership
import com.bank.user.entity.User
import jakarta.persistence.*
import java.time.LocalDateTime

@Entity
@Table(name = "user_memberships")
data class UserMembership(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    val user: User,

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "account_id", nullable = false)
    val account: Account,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "membership_tier_id", nullable = false)
    val membershipTier: Membership,

    @Column(nullable = false)
    val startedAt: LocalDateTime,

    @Column
    val endedAt: LocalDateTime? = null,

    @Column(nullable = false)
    val tierPoints: Int = 0
)

data class UserMembershipRequest(
    val userId: Long,
    val accountId: Long,
    val membershipTierId: Long,
    val startedAt: LocalDateTime,
    val endedAt: LocalDateTime?,
    val tierPoints: Int
)


data class UserMembershipResponse(
    val id: Long,
    val userId: Long,
    val username: String,
    val accountId: Long,
    val membershipTierName: String,
    val startedAt: LocalDateTime,
    val endedAt: LocalDateTime?,
    val tierPoints: Int
)