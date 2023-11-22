package de.rosenau.simon.rockpaperscissors.util

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder

object Passwords {
    private val encoder = BCryptPasswordEncoder()
    fun hash(password: String): String {
        return encoder.encode(password)
    }

    fun verify(password: String, hash: String): Boolean {
        return encoder.matches(password, hash)
    }
}