package de.rosenau.simon.rockpaperscissors.domain.game.api.command

import de.rosenau.simon.rockpaperscissors.domain.game.api.Player
import org.axonframework.modelling.command.TargetAggregateIdentifier
import java.util.*

data class IntentGameCommand(
    @TargetAggregateIdentifier val gameId: UUID = UUID.randomUUID(),
    val playerId: Player.Id,
    val computer: Boolean,
)