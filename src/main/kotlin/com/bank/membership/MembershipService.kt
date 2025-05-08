package com.bank.membership

import com.bank.kyc.KYCResponse
import com.bank.mcCacheConfig
import com.bank.serverMcCache
import com.hazelcast.logging.Logger
import org.springframework.stereotype.Service
import java.util.*
import kotlin.NoSuchElementException
private val loggerTiers = Logger.getLogger("tiers")


@Service
class MembershipService(
    private val membershipRepository: MembershipRepository
) {
    fun getAll(userId: Long?): List<MembershipTierEntity> {
        val tiersCache = serverMcCache.getMap<Long, List<MembershipTierEntity>>("tiers")

        tiersCache[userId]?.let {
            loggerTiers.info("returning list of tiers from cache")
            return it
        }
        loggerTiers.info("no tiers list found...caching new data")
        tiersCache[userId] = membershipRepository.findAll()
        return membershipRepository.findAll()
    }

    fun getByTierName(tierName: String): MembershipTierEntity? {
        return try {
            val tierNameUpper = tierName.uppercase(Locale.getDefault())
            membershipRepository.findByTierName(tierNameUpper)
        } catch (e: IllegalArgumentException) {
            null
        }
    }
}