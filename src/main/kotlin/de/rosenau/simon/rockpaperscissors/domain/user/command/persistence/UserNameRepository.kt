package de.rosenau.simon.rockpaperscissors.domain.user.command.persistence

import org.springframework.data.jpa.repository.JpaRepository

interface UserNameRepository: JpaRepository<UserNameJpaEntity, String> {
    fun existsByUsername(name: String): Boolean
}