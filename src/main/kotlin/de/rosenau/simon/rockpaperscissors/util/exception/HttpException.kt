package de.rosenau.simon.rockpaperscissors.util.exception

import org.springframework.http.HttpStatus

open class HttpException(
    val status: HttpStatus,
    override val message: String,
): RuntimeException(message)