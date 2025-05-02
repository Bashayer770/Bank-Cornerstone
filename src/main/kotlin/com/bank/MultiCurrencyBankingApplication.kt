package com.bank

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.autoconfigure.domain.EntityScan
import org.springframework.boot.runApplication
import org.springframework.data.jpa.repository.config.EnableJpaRepositories

@SpringBootApplication
@EntityScan("com.bank.*.entity")
@EnableJpaRepositories("com.bank.*.repository")
class MultiCurrencyBankingApplication

fun main(args: Array<String>) {
    runApplication<MultiCurrencyBankingApplication>(*args)
} 