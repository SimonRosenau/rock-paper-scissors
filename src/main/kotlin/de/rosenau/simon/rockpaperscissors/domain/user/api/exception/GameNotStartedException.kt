package de.rosenau.simon.rockpaperscissors.domain.user.api.exception

import de.rosenau.simon.rockpaperscissors.util.exception.HttpException
import org.springframework.http.HttpStatus
import java.util.*

class GameNotStartedException(identifier: UUID): HttpException(HttpStatus.BAD_REQUEST, "Game not started: $identifier")