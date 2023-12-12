package de.rosenau.simon.rockpaperscissors.domain.user.api.dto

import jakarta.validation.constraints.NotEmpty
import jakarta.validation.constraints.Pattern

data class CreateUserDto(
    @field:NotEmpty(message = "Username must not be empty")
    val username: String,
    @field:NotEmpty(message = "Password must not be empty")
    @field:Pattern(
        regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)\\S{8,}$",
        message = "Password must be at least 8 characters long and contain at least one uppercase letter, one lowercase letter and one number"
    )
    val password: String,
)