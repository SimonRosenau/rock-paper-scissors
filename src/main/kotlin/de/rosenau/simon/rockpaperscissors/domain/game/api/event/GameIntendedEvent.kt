package de.rosenau.simon.rockpaperscissors.domain.game.api.event

import de.rosenau.simon.rockpaperscissors.domain.game.api.Player
import org.axonframework.modelling.command.TargetAggregateIdentifier
import java.util.*

data class GameIntendedEvent(
    @TargetAggregateIdentifier val gameId: UUID,
    val playerId: Player.Id,
    val computer: Boolean
)
