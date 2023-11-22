package de.rosenau.simon.rockpaperscissors.domain.user.api.event

import org.axonframework.modelling.command.TargetAggregateIdentifier
import java.util.*

data class UserRegisteredEvent(
    @TargetAggregateIdentifier val userId: UUID,
    val username: String,
    val password: String,
)
