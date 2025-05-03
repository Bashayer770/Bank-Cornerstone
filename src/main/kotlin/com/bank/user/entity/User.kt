package com.bank.user.entity

import com.bank.account.entity.Account
import com.bank.kyc.entity.KYC
import com.bank.role.entity.Role
import com.bank.usermembership.entity.UserMembership
import jakarta.persistence.*
import java.time.LocalDateTime

@Entity
@Table(name = "users")
data class User(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    @Column(nullable = false, unique = true)
    val username: String,

    @Column(nullable = false)
    val password: String,

    @Column(nullable = false)
    val createdAt: LocalDateTime = LocalDateTime.now(),

    @OneToMany(mappedBy = "user", cascade = [CascadeType.ALL])
    val accounts: MutableList<Account> = mutableListOf(),

    @OneToMany(mappedBy = "user", cascade = [CascadeType.ALL])
    val roles: MutableList<Role> = mutableListOf(),

    @OneToMany(mappedBy = "user", cascade = [CascadeType.ALL])
    val memberships: MutableList<UserMembership> = mutableListOf(),

    @OneToOne(mappedBy = "user", cascade = [CascadeType.ALL])
    val kyc: KYC? = null
) 