package com.bank.currency.repository

import com.bank.currency.entity.Currency
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface CurrencyRepository : JpaRepository<Currency, Long> {
    fun findByCountryCode(countryCode: String): Currency?
    fun findBySymbol(symbol: String): Currency?
} 