package com.bank.config

import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Primary
import org.springframework.context.annotation.Profile
import org.springframework.web.client.RestTemplate
import org.mockito.Mockito
import com.bank.exchange.ExchangeRateApi

@TestConfiguration
@Profile("test")
class TestExchangeRateConfig {
    @Bean
    @Primary
    fun mockRestTemplate(): RestTemplate {
        return Mockito.mock(RestTemplate::class.java)
    }

    @Bean
    @Primary
    fun mockExchangeRateApi(): ExchangeRateApi {
        return Mockito.mock(ExchangeRateApi::class.java)
    }
} 