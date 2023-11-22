package de.rosenau.simon.rockpaperscissors.domain.game.api.dto

import de.rosenau.simon.rockpaperscissors.domain.game.api.Hand

object SelectHandDto {
    data class Request(
        val hand: Hand,
    )
}