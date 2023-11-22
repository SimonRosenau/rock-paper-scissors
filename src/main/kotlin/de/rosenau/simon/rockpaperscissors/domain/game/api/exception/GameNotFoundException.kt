package de.rosenau.simon.rockpaperscissors.domain.game.api.exception

import de.rosenau.simon.rockpaperscissors.util.exception.HttpException
import org.springframework.http.HttpStatus
import java.util.*

class GameNotFoundException(id: UUID): HttpException(HttpStatus.NOT_FOUND, "Game not found: $id")