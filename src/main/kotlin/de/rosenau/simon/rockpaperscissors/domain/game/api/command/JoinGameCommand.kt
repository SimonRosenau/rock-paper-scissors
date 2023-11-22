package de.rosenau.simon.rockpaperscissors.domain.game.api.command

import de.rosenau.simon.rockpaperscissors.domain.game.api.Player
import org.axonframework.modelling.command.TargetAggregateIdentifier
import java.util.*

data class JoinGameCommand(
    @TargetAggregateIdentifier val gameId: UUID,
    val playerId: Player.Id
)