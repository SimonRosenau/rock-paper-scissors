package de.rosenau.simon.rockpaperscissors.domain.user.api.command

import org.axonframework.modelling.command.TargetAggregateIdentifier
import java.util.*

data class RegisterUserCommand(
    @TargetAggregateIdentifier val userId: UUID = UUID.randomUUID(),
    val username: String,
    val password: String,
)