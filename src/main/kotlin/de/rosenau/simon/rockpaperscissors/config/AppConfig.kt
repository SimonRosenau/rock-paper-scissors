package de.rosenau.simon.rockpaperscissors.config

import org.springframework.boot.autoconfigure.domain.EntityScan
import org.springframework.context.annotation.Configuration

@Configuration
@EntityScan("de.rosenau.simon.rockpaperscissors")
class AppConfig