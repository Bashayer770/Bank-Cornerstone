package com.bank.membership

import com.bank.membership.MembershipService
import com.bank.membership.entity.Membership
import org.springframework.web.bind.annotation.*


@RestController
@RequestMapping("/api/memberships")
class MembershipController(
    private val membershipService: MembershipService
) {
    @GetMapping
    fun getAll(): List<Membership> = membershipService.getAll()

    @GetMapping("/{id}")
    fun getById(@PathVariable id: Long): Membership = membershipService.getById(id)

    @GetMapping("/tier/{name}")
    fun getByTierName(@PathVariable name: String): Membership? = membershipService.getByTierName(name)
}