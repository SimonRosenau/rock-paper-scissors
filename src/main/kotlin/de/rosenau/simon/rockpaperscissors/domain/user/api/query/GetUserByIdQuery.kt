package de.rosenau.simon.rockpaperscissors.domain.user.api.query

import java.util.*

data class GetUserByIdQuery(
    val userId: UUID,
)
