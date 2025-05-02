package com.bank.role.repository

import com.bank.role.entity.Role
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface RoleRepository : JpaRepository<Role, Long> {
    fun findByUserId(userId: Long): List<Role>
    fun findByName(name: String): Role?
} 