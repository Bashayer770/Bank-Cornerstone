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

    @PostMapping
    fun create(@RequestBody membership: Membership): Membership = membershipService.create(membership)
}