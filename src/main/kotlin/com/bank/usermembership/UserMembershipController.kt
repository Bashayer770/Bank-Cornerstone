package com.bank.usermembership

import com.bank.usermembership.entity.UserMembershipRequest
import com.bank.usermembership.entity.UserMembershipResponse
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/user-memberships")
class UserMembershipController(
    private val service: UserMembershipService
) {

    @GetMapping
    fun getAll(): List<UserMembershipResponse> =
        service.getAll().map { service.toResponse(it) }

    @GetMapping("/user/{userId}")
    fun getByUserId(@PathVariable userId: Long): List<UserMembershipResponse> =
        service.getByUserId(userId).map { service.toResponse(it) }

    @PostMapping
    fun create(@RequestBody request: UserMembershipRequest): UserMembershipResponse {
        val created = service.create(request)
        return service.toResponse(created)
    }
}
