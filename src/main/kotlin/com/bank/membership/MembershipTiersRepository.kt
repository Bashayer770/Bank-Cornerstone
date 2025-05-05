package com.bank.membership

import jakarta.persistence.*
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.math.BigDecimal

@Repository
interface MembershipRepository : JpaRepository<MembershipEntity, Long> {
    //fun findByName(name: String): MembershipEntity?
}

@Entity
@Table(name = "memberships")
data class MembershipEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    @Column(name = "tier_name")
    val tierName: String,

    @Column(name = "member_limit")
    val memberLimit: Int,

    @Column(name = "discount_amount", precision = 9, scale = 3)
    val discountAmount: BigDecimal
) {
    constructor() : this(null, "BRONZE", 0, BigDecimal("0.050"))
}