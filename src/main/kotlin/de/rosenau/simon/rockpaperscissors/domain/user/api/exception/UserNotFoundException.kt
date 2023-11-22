package de.rosenau.simon.rockpaperscissors.domain.user.api.exception

import de.rosenau.simon.rockpaperscissors.util.exception.HttpException
import org.springframework.http.HttpStatus

class UserNotFoundException(identifier: String): HttpException(HttpStatus.NOT_FOUND, "User not found: $identifier")