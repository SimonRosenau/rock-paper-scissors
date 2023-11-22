package de.rosenau.simon.rockpaperscissors.domain.user.api.query

data class GetUserByCorrelationIdQuery(
    val correlationId: String,
)
