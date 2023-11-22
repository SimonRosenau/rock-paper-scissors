package de.rosenau.simon.rockpaperscissors.domain.user.command.persistence

import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.util.*

@Entity
@Table(name = "user_names")
data class UserNameJpaEntity(
    @Id val username: String,
    val userId: UUID,
)