package de.rosenau.simon.rockpaperscissors.domain.game.query

import de.rosenau.simon.rockpaperscissors.domain.game.query.model.Game
import org.springframework.data.jpa.repository.JpaRepository
import java.util.*

interface GameRepository: JpaRepository<Game, UUID>