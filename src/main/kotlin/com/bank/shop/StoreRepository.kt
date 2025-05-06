package com.bank.shop

import com.bank.membership.entity.MembershipTier
import com.bank.shop.StoreItem
import org.springframework.data.jpa.repository.JpaRepository

interface StoreRepository : JpaRepository<StoreItem, Long> {
    fun findByTierIn(tiers: List<MembershipTier>): List<StoreItem>
}