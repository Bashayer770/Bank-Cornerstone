package com.bank.membership.entity

import jakarta.persistence.*

@Entity
@Table(name = "memberships")
data class Membership(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,

    @Column(nullable = false, unique = true)
    val name: String,

    @Column(nullable = false)
    val memberLimit: Int,

    @Column(nullable = false)
    val discountAmount: Double
) 