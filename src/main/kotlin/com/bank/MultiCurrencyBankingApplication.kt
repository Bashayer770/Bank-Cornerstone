package com.bank

import com.hazelcast.config.Config
import com.hazelcast.core.Hazelcast
import com.hazelcast.core.HazelcastInstance
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.autoconfigure.domain.EntityScan
import org.springframework.boot.runApplication
import org.springframework.data.jpa.repository.config.EnableJpaRepositories

@SpringBootApplication
class MultiCurrencyBankingApplication

fun main(args: Array<String>) {
    runApplication<MultiCurrencyBankingApplication>(*args)
    kycsConfig.getMapConfig("kycs").setTimeToLiveSeconds(60)
    accountsConfig.getMapConfig("accounts").setTimeToLiveSeconds(60)
}

val kycsConfig = Config("kyc-cache")
val serverKycsCache: HazelcastInstance = Hazelcast.newHazelcastInstance(kycsConfig)

val accountsConfig = Config("accounts-cache")
val serverAccountsCache: HazelcastInstance = Hazelcast.newHazelcastInstance(accountsConfig)