package com.bank.shop

import com.bank.membership.entity.MembershipTier
import com.bank.usermembership.repository.UserMembershipRepository
import org.springframework.stereotype.Service

@Service
class StoreService(
    private val storeRepository: StoreRepository,
    private val userMembershipRepository: UserMembershipRepository
) {

    fun getStoreItemsForUser(userId: Long): List<StoreItem> {
        val memberships = userMembershipRepository.findByUserId(userId)

        val highestTier = memberships
            .map { it.membershipTier.name }
            .maxByOrNull { it.ordinal } ?: MembershipTier.BRONZE

        val visibleTiers = MembershipTier.entries.filter { it.ordinal <= highestTier.ordinal }

        return storeRepository.findByTierIn(visibleTiers)
    }

    fun purchaseItem(userId: Long, itemId: Long): PurchaseResponse {
        val item = storeRepository.findById(itemId)
            .orElseThrow { NoSuchElementException("Item with ID $itemId not found") }

        // needs actual logic

        return PurchaseResponse(
            userId = userId,
            item = item
        )
    }
}
