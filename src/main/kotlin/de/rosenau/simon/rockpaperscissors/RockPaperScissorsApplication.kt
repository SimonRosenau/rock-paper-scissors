package de.rosenau.simon.rockpaperscissors

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class RockPaperScissorsApplication

fun main(args: Array<String>) {
    System.setProperty("disable-axoniq-console-message", "true")
    runApplication<RockPaperScissorsApplication>(*args)
}
