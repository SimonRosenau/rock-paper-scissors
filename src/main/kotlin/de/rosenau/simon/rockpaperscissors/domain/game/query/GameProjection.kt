package de.rosenau.simon.rockpaperscissors.domain.game.query

import de.rosenau.simon.rockpaperscissors.domain.game.api.GameState
import de.rosenau.simon.rockpaperscissors.domain.game.api.event.*
import de.rosenau.simon.rockpaperscissors.domain.game.api.exception.GameNotFoundException
import de.rosenau.simon.rockpaperscissors.domain.game.api.query.GetGameByCorrelationIdQuery
import de.rosenau.simon.rockpaperscissors.domain.game.api.query.GetGameByIdQuery
import de.rosenau.simon.rockpaperscissors.domain.game.query.model.Game
import org.axonframework.eventhandling.EventHandler
import org.axonframework.messaging.annotation.MetaDataValue
import org.axonframework.queryhandling.QueryHandler
import org.axonframework.queryhandling.QueryUpdateEmitter
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import java.util.*

@Component
class GameProjection {
    @Autowired
    private lateinit var queryUpdateEmitter: QueryUpdateEmitter

    @Autowired
    private lateinit var gameRepository: GameRepository

    // EventHandlers

    @EventHandler
    fun on(event: GameIntendedEvent, @MetaDataValue("correlationId") correlationId: String) {
        val game = Game(event.gameId, GameState.CREATED, mutableSetOf(event.playerId.toNewPlayer()), 0)
        gameRepository.save(game)
        notifyGameUpdate(game, correlationId)
    }

    @EventHandler
    fun on(event: GameJoinedEvent, @MetaDataValue("correlationId") correlationId: String) {
        val game = gameRepository.findById(event.gameId).orElseThrow { GameNotFoundException(event.gameId) }
        game.players.add(event.playerId.toNewPlayer())
        gameRepository.save(game)
        notifyGameUpdate(game, correlationId)
    }

    @EventHandler
    fun on(event: GamePlayerReadyEvent, @MetaDataValue("correlationId") correlationId: String) {
        val game = gameRepository.findById(event.gameId).orElseThrow { GameNotFoundException(event.gameId) }
        game.updatePlayer(event.playerId) { it.copy(ready = true) }
        gameRepository.save(game)
        notifyGameUpdate(game, correlationId)
    }

    @EventHandler
    fun on(event: GameRoundStartedEvent, @MetaDataValue("correlationId") correlationId: String) {
        val game = gameRepository.findById(event.gameId).orElseThrow { GameNotFoundException(event.gameId) }
        game.state = GameState.STARTED
        game.round += 1
        game.updatePlayers { it.copy(hand = null) }
        gameRepository.save(game)
        notifyGameUpdate(game, correlationId)
    }

    @EventHandler
    fun on(event: HandSelectedEvent, @MetaDataValue("correlationId") correlationId: String) {
        val game = gameRepository.findById(event.gameId).orElseThrow { GameNotFoundException(event.gameId) }
        game.updatePlayer(event.playerId) { it.copy(hand = event.hand) }
        gameRepository.save(game)
        notifyGameUpdate(game, correlationId)
    }

    @EventHandler
    fun on(event: PlayerScoredEvent, @MetaDataValue("correlationId") correlationId: String) {
        val game = gameRepository.findById(event.gameId).orElseThrow { GameNotFoundException(event.gameId) }
        game.updatePlayer(event.playerId) { it.copy(score = it.score + event.score) }
        gameRepository.save(game)
        notifyGameUpdate(game, correlationId)
    }

    @EventHandler
    fun on(event: GameFinishedEvent, @MetaDataValue("correlationId") correlationId: String) {
        val game = gameRepository.findById(event.gameId).orElseThrow { GameNotFoundException(event.gameId) }
        game.state = GameState.FINISHED
        gameRepository.save(game)
        notifyGameUpdate(game, correlationId)
    }

    // QueryHandlers

    @QueryHandler
    fun handle(query: GetGameByCorrelationIdQuery): Optional<Void> {
        // Later resolved through the queryUpdateEmitter
        return Optional.empty()
    }

    @QueryHandler
    fun handle(query: GetGameByIdQuery): Game {
        return gameRepository.findById(query.gameId)
            .orElseThrow { GameNotFoundException(query.gameId) }
    }

    /**
     * Notify all queries that are waiting for an update of the given game.
     * @param game The game that was updated.
     * @param correlationId The correlationId of the query that should be notified.
     */
    private fun notifyGameUpdate(game: Game, correlationId: String) {
        queryUpdateEmitter.emit(GetGameByCorrelationIdQuery::class.java, { it.correlationId == correlationId }, game)
        queryUpdateEmitter.emit(GetGameByIdQuery::class.java, { it.gameId == game.id }, game)
    }
}