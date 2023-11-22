package de.rosenau.simon.rockpaperscissors.domain.game.api.command

import de.rosenau.simon.rockpaperscissors.domain.game.api.Hand
import de.rosenau.simon.rockpaperscissors.domain.game.api.Player
import org.axonframework.modelling.command.TargetAggregateIdentifier
import java.util.*

data class SelectHandCommand(
    @TargetAggregateIdentifier val gameId: UUID,
    val playerId: Player.Id,
    val hand: Hand
)