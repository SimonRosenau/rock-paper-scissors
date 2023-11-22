package de.rosenau.simon.rockpaperscissors.domain.user.api.exception

import de.rosenau.simon.rockpaperscissors.util.exception.HttpException
import org.springframework.http.HttpStatus

class UserNameAlreadyExistsException(name: String):
    HttpException(HttpStatus.CONFLICT, "User name already exists: $name")