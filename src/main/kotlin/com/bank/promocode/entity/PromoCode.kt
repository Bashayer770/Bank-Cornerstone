package com.bank.promocode.entity

import jakarta.persistence.*

@Entity
@Table(name = "promo_codes")
data class PromoCode(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,

    @Column(nullable = false, unique = true)
    val code: String,

    @Column(nullable = false)
    val description: String
) 