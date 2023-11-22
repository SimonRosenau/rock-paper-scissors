package de.rosenau.simon.rockpaperscissors.domain.game.api

import java.util.function.Function

enum class Hand(val beats: Function<Hand, Boolean>) {
    ROCK({ it == SCISSORS }),
    PAPER({ it == ROCK }),
    SCISSORS({ it == PAPER }),
}