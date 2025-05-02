package com.bank.currency.entity

import jakarta.persistence.*

@Entity
@Table(name = "currencies")
data class Currency(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,

    @Column(nullable = false, unique = true)
    val countryCode: String,

    @Column(nullable = false)
    val symbol: String,

    @Column(nullable = false)
    val conversionRate: Double
) 