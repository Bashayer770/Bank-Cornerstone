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
    mcCacheConfig.getMapConfig("kyc").setTimeToLiveSeconds(60)
    mcCacheConfig.getMapConfig("account").setTimeToLiveSeconds(60)
}

val mcCacheConfig = Config("multi-currency-cache")
val serverMcCache: HazelcastInstance = Hazelcast.newHazelcastInstance(mcCacheConfig)
