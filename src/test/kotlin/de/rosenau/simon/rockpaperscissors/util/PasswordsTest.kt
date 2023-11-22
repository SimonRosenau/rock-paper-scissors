package de.rosenau.simon.rockpaperscissors.util

import org.junit.jupiter.api.Test

class PasswordsTest {

    /**
     * Generate 3 random passwords and test them against 10 other random passwords.
     * Both hashing and verifying should work.
     */
    @Test
    fun test() {
        val passwords = (1..3).map { generatePassword() }
        val others = (1..3).map { generatePassword() }
        passwords.forEach { password ->
            val hash = Passwords.hash(password)
            assert(Passwords.verify(password, hash))
            others.forEach { other ->
                assert(!Passwords.verify(other, hash))
            }
        }
    }

    private fun generatePassword(): String {
        return (1..10).map { ('a'..'z').random() }.joinToString("")
    }
}