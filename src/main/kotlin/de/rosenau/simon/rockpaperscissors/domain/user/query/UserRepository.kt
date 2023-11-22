package de.rosenau.simon.rockpaperscissors.domain.user.query

import de.rosenau.simon.rockpaperscissors.domain.user.query.model.User
import org.springframework.data.jpa.repository.JpaRepository
import java.util.*

interface UserRepository: JpaRepository<User, UUID> {
    fun findByUsername(username: String): Optional<User>
}