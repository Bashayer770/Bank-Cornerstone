package com.bank.config

import com.bank.authentication.jwt.JwtService
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.SignatureAlgorithm
import io.jsonwebtoken.security.Keys
import org.springframework.context.annotation.Primary
import org.springframework.stereotype.Component
import java.util.*

@Component
@Primary
class TestJwtService : JwtService() {
    // Use a fixed secret key for testing
    private val secretKey = Keys.hmacShaKeyFor("test_secret_key_that_is_long_enough_for_hs256_algorithm".toByteArray())
    private val expirationMs: Long = 1000 * 60 * 60 // 1 hour

    override fun generateToken(username: String): String {
        val now = Date()
        val expiry = Date(now.time + expirationMs)

        return Jwts.builder()
            .setSubject(username)
            .setIssuedAt(now)
            .setExpiration(expiry)
            .signWith(secretKey)
            .compact()
    }

    override fun extractUsername(token: String): String =
        Jwts.parserBuilder()
            .setSigningKey(secretKey)
            .build()
            .parseClaimsJws(token)
            .body
            .subject

    override fun isTokenValid(token: String, username: String): Boolean {
        return try {
            extractUsername(token) == username
        } catch (e: Exception) {
            false
        }
    }
} 