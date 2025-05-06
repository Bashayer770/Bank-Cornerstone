package com.bank

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.autoconfigure.domain.EntityScan
import org.springframework.data.jpa.repository.config.EnableJpaRepositories
import org.springframework.boot.runApplication

@SpringBootApplication
@EntityScan("com.bank")
@EnableJpaRepositories("com.bank")
class MultiCurrencyBankingApplication
fun main(args: Array<String>) {
    runApplication<MultiCurrencyBankingApplication>(*args)
}
