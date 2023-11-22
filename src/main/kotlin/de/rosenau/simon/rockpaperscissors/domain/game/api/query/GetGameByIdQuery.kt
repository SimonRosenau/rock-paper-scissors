package de.rosenau.simon.rockpaperscissors.domain.game.api.query

import java.util.*

data class GetGameByIdQuery(
    val gameId: UUID,
)
