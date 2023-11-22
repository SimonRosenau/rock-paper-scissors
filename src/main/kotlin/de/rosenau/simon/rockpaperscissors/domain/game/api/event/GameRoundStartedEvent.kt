package de.rosenau.simon.rockpaperscissors.domain.game.api.event

import org.axonframework.modelling.command.TargetAggregateIdentifier
import java.util.*

data class GameRoundStartedEvent(
    @TargetAggregateIdentifier val gameId: UUID,
)
