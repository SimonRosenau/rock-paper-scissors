package de.rosenau.simon.rockpaperscissors.domain.user.api.exception

import de.rosenau.simon.rockpaperscissors.util.exception.HttpException
import org.springframework.http.HttpStatus
import java.util.*

class PlayerAlreadySelectedHandException(identifier: UUID):
    HttpException(HttpStatus.BAD_REQUEST, "Player already selected the hand in game: $identifier")