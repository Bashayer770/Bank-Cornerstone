package com.bank.usermembership

import com.bank.account.AccountEntity
import com.bank.membership.MembershipEntity
import com.bank.user.UserEntity
import jakarta.persistence.*
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.time.LocalDateTime

@Repository
interface UserMembershipRepository : JpaRepository<UserMembershipEntity, Long> {
    fun findByUserId(userId: Long): List<UserMembershipEntity>
    fun findByAccountId(accountId: Long): UserMembershipEntity?
    fun findByMembershipTierId(membershipTierId: Long): List<UserMembershipEntity>
    fun findByStartedAtBetween(start: LocalDateTime, end: LocalDateTime): List<UserMembershipEntity>
    fun findByEndedAtIsNull(): List<UserMembershipEntity>
}

@Entity
@Table(name = "user_memberships")
data class UserMembershipEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", referencedColumnName = "id")
    val user: UserEntity,

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "account_id", referencedColumnName = "id")
    val account: AccountEntity,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "membership_tiers_id", referencedColumnName = "id")
    val membershipTier: MembershipEntity,

    @Column(name = "started_at")
    val startedAt: LocalDateTime? = null,

    @Column(name = "ended_at")
    val endedAt: LocalDateTime? = null,

    @Column(nullable = false)
    val tierPoints: Int = 0
) {
    constructor() : this(null, UserEntity(), AccountEntity(), MembershipEntity(), null, null, 0)
}