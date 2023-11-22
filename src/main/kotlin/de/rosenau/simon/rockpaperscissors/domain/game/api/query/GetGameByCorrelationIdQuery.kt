package de.rosenau.simon.rockpaperscissors.domain.game.api.query

data class GetGameByCorrelationIdQuery(
    val correlationId: String,
)
