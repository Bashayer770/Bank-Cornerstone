package com.bank.membership.entity

import jakarta.persistence.*

@Entity
@Table(name = "memberships")
data class Membership(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, unique = true)
    val name: MembershipTier,

    @Column(nullable = false)
    val memberLimit: Int,

    @Column(nullable = false)
    val discountAmount: Double //may change to bigdecimal according to schema
)

enum class MembershipTier {
    BRONZE,
    SILVER,
    GOLD
}