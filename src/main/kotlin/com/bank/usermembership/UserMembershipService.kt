package com.bank.usermembership

import com.bank.account.repository.AccountRepository
import com.bank.membership.entity.MembershipTier
import com.bank.membership.repository.MembershipRepository
import com.bank.user.repository.UserRepository
import com.bank.usermembership.entity.*
import com.bank.usermembership.entity.UserMembership
import com.bank.usermembership.entity.UserMembershipRequest
import com.bank.usermembership.entity.UserMembershipResponse
import com.bank.usermembership.repository.UserMembershipRepository
import org.springframework.stereotype.Service

@Service
class UserMembershipService(
    private val userMembershipRepository: UserMembershipRepository,
    private val userRepository: UserRepository,
    private val accountRepository: AccountRepository,
    private val membershipRepository: MembershipRepository
) {

    fun create(request: UserMembershipRequest): UserMembership {
        val user = userRepository.findById(request.userId)
            .orElseThrow { IllegalArgumentException("User with ID ${request.userId} not found") }

        val account = accountRepository.findById(request.accountId)
            .orElseThrow { IllegalArgumentException("Account with ID ${request.accountId} not found") }

        val membershipTier = when {
            request.tierPoints >= 5000 -> MembershipTier.GOLD
            request.tierPoints >= 3000 -> MembershipTier.SILVER
            else -> MembershipTier.BRONZE
        }

        val membership = membershipRepository.findByName(membershipTier)
            ?: throw IllegalStateException("Membership tier $membershipTier not found in DB")

        val newMembership = UserMembership(
            user = user,
            account = account,
            membershipTier = membership,
            membershipTierName = membership.name.toString(),
            startedAt = request.startedAt,
            endedAt = request.endedAt,
            tierPoints = request.tierPoints
        )

        return userMembershipRepository.save(newMembership)
    }

    fun create(userMembership: UserMembership): UserMembership =
        userMembershipRepository.save(userMembership)

    fun getAll(): List<UserMembership> =
        userMembershipRepository.findAll()

    fun getByUserId(userId: Long): List<UserMembership> =
        userMembershipRepository.findByUserId(userId)

    fun toResponse(entity: UserMembership): UserMembershipResponse {
        return UserMembershipResponse(
            id = entity.id,
            userId = entity.user.id ?: -1,
            username = entity.user.username,
            accountId = entity.account.id,
            membershipTierName = entity.membershipTierName,
            startedAt = entity.startedAt,
            endedAt = entity.endedAt,
            tierPoints = entity.tierPoints
        )
    }
}