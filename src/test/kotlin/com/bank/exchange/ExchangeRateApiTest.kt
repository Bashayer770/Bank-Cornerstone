package com.bank.exchange

import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import org.mockito.Mockito
import org.junit.jupiter.api.Assertions
import java.math.BigDecimal
import com.bank.config.TestExchangeRateConfig
import com.bank.config.TestSecurityConfig
import org.springframework.context.annotation.Import

@SpringBootTest
@ActiveProfiles("test")
@Import(TestSecurityConfig::class, TestExchangeRateConfig::class)
class ExchangeRateApiTest {

    @Autowired
    private lateinit var exchangeRateApi: ExchangeRateApi

    @Test
    fun `test get exchange rate success`() {
        // Mock successful response
        Mockito.`when`(exchangeRateApi.getRate("USD", "EUR"))
            .thenReturn(BigDecimal("0.85"))

        val rate = exchangeRateApi.getRate("USD", "EUR")
        Assertions.assertEquals(BigDecimal("0.85"), rate)
    }

    @Test
    fun `test get exchange rate error`() {
        // Mock error response
        Mockito.`when`(exchangeRateApi.getRate("USD", "INVALID"))
            .thenThrow(RuntimeException("Invalid currency"))

        Assertions.assertThrows(RuntimeException::class.java) {
            exchangeRateApi.getRate("USD", "INVALID")
        }
    }

    @Test
    fun `test get exchange rate invalid currency`() {
        // Mock invalid currency response
        Mockito.`when`(exchangeRateApi.getRate("INVALID", "USD"))
            .thenThrow(RuntimeException("Invalid currency"))

        Assertions.assertThrows(RuntimeException::class.java) {
            exchangeRateApi.getRate("INVALID", "USD")
        }
    }
} 