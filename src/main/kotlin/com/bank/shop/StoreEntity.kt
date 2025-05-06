package com.bank.shop

import com.bank.membership.entity.MembershipTier
import jakarta.persistence.*

@Entity
@Table(name = "store_items")
data class StoreItem(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,

    @Column(nullable = false)
    val name: String,

    @Column(nullable = false)
    val pointCost: Int,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    val tier: MembershipTier
)

data class PurchaseRequest(
    val itemId: Long
)

data class PurchaseResponse(
    val userId: Long,
    val item: StoreItem
)