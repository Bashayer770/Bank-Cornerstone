package com.bank.currency

import jakarta.persistence.*
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface CurrencyRepository : JpaRepository<CurrencyEntity, Long> {
//    fun findByCountryCode(countryCode: String): CurrencyEntity?
//    fun findBySymbol(symbol: String): CurrencyEntity?
}

@Entity
@Table(name = "currencies")
data class CurrencyEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    @Column(name = "country_code")
    val countryCode: String,

    val symbol: String

) {
    constructor() : this(null, "", "")
}