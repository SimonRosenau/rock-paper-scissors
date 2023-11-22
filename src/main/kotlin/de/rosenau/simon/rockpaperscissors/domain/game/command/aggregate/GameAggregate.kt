package de.rosenau.simon.rockpaperscissors.domain.game.command.aggregate

import de.rosenau.simon.rockpaperscissors.domain.game.api.GameState
import de.rosenau.simon.rockpaperscissors.domain.game.api.Player
import de.rosenau.simon.rockpaperscissors.domain.game.api.command.IntentGameCommand
import de.rosenau.simon.rockpaperscissors.domain.game.api.command.JoinGameCommand
import de.rosenau.simon.rockpaperscissors.domain.game.api.command.MarkPlayerReadyCommand
import de.rosenau.simon.rockpaperscissors.domain.game.api.command.SelectHandCommand
import de.rosenau.simon.rockpaperscissors.domain.game.api.event.*
import de.rosenau.simon.rockpaperscissors.domain.user.api.exception.*
import org.axonframework.commandhandling.CommandHandler
import org.axonframework.eventsourcing.EventSourcingHandler
import org.axonframework.modelling.command.AggregateIdentifier
import org.axonframework.modelling.command.AggregateLifecycle
import org.axonframework.spring.stereotype.Aggregate
import java.util.*

@Aggregate
class GameAggregate {
    @AggregateIdentifier
    lateinit var gameId: UUID
    lateinit var state: GameState

    /**
     * The data structure, event handling and domain logic already supports more than two players.
     * However, the UI does not (for now). Therefor the backend also initializes games with only two players and also always a computer opponent.
     */
    lateinit var players: MutableSet<Player>

    /**
     * The number of players in a game.
     * Currently this is always 2, as the UI does not support more players.
     */
    private val size: Int = 2
    private val rounds: Int = 3 // Maybe this can be configured in the future

    @CommandHandler
    constructor(command: IntentGameCommand) {
        AggregateLifecycle.apply(GameIntendedEvent(command.gameId, command.playerId, command.computer))
    }

    @EventSourcingHandler
    fun on(event: GameIntendedEvent) {
        gameId = event.gameId
        state = GameState.CREATED
        players = mutableSetOf(event.playerId.toNewPlayer())
    }

    @CommandHandler
    fun handle(command: JoinGameCommand) {
        if (state != GameState.CREATED) throw GameAlreadyStartedException(command.gameId)
        if (players.size >= size) throw GameAlreadyFullException(command.gameId)
        if (players.any { it.matches(command.playerId) }) throw PlayerAlreadyJoinedException(command.gameId)

        AggregateLifecycle.apply(GameJoinedEvent(command.gameId, command.playerId))
    }

    @EventSourcingHandler
    fun on(event: GameJoinedEvent) {
        players.add(event.playerId.toNewPlayer())
    }

    @CommandHandler
    fun handle(command: MarkPlayerReadyCommand) {
        val player = resolvePlayer(command.playerId)
        if (player.ready) throw PlayerAlreadyReadyException(command.gameId)
        AggregateLifecycle.apply(GamePlayerReadyEvent(command.gameId, command.playerId))
    }

    @EventSourcingHandler
    fun on(event: GamePlayerReadyEvent) {
        updatePlayer(event.playerId) { it.copy(ready = true) }

        react {
            // Start the game if all players have joined and are ready
            if (players.size == size && players.all { it.ready }) {
                AggregateLifecycle.apply(GameRoundStartedEvent(event.gameId))
            }
        }
    }

    @EventSourcingHandler
    fun on(event: GameRoundStartedEvent) {
        state = GameState.STARTED
        updatePlayers { it.copy(hand = null) }
    }

    @CommandHandler
    fun handle(command: SelectHandCommand) {
        val player = resolvePlayer(command.playerId)
        if (state != GameState.STARTED) throw GameNotStartedException(command.gameId)
        if (player.hand != null) throw PlayerAlreadySelectedHandException(command.gameId)
        AggregateLifecycle.apply(HandSelectedEvent(command.gameId, command.playerId, command.hand))
    }

    @EventSourcingHandler
    fun on(event: HandSelectedEvent) {
        updatePlayer(event.playerId) { it.copy(hand = event.hand) }

        react {
            // Check if all players have selected a hand
            if (players.all { it.hand != null }) {
                // Calculate the score for each player, based on how many other players they beat
                val scores: Map<Player, Int> = players.associateWith { participant ->
                    players.count { participant.hand!!.beats.apply(it.hand!!) }
                }
                // Emit events for each player that scored
                scores.forEach { (player, score) ->
                    if (score > 0) {
                        AggregateLifecycle.apply(PlayerScoredEvent(event.gameId, player.toId(), score))
                    }
                }
                // Check if a player has won the game
                if (players.any { it.score + scores.getOrDefault(it, 0) >= rounds }) {
                    // Finish the game
                    AggregateLifecycle.apply(GameFinishedEvent(event.gameId))
                } else {
                    // Start new round
                    AggregateLifecycle.apply(GameRoundStartedEvent(event.gameId))
                }
            }
        }
    }

    @EventSourcingHandler
    fun on(event: PlayerScoredEvent) {
        updatePlayer(event.playerId) { it.copy(score = it.score + event.score) }
    }

    @EventSourcingHandler
    fun on(event: GameFinishedEvent) {
        state = GameState.FINISHED
    }

    /**
     * Helper function to resolve a player from an id.
     * @param player The id of the player to resolve
     * @return The player
     * @throws IllegalArgumentException if the player could not be found
     */
    private fun resolvePlayer(player: Player.Id): Player {
        return players.find { it.matches(player) } ?: throw IllegalArgumentException("Player not found: $player")
    }

    /**
     * Updates the player with the given id by applying the given update function.
     * If no player with the given id exists, nothing happens.
     * @param playerId the id of the player to update
     * @param update the update function to apply
     * @return this game aggregate (for method chaining)
     */
    private fun updatePlayer(playerId: Player.Id, update: (Player) -> Player): GameAggregate {
        players = players.map { if (it.matches(playerId)) update(it) else it }.toMutableSet()
        return this
    }

    /**
     * Updates all players by applying the given update function.
     * @param update the update function to apply
     * @return this game aggregate (for method chaining)
     */
    private fun updatePlayers(update: (Player) -> Player): GameAggregate {
        players = players.map { update(it) }.toMutableSet()
        return this
    }

    /**
     * Helper function to only react to events if the aggregate is live.
     * This is necessary because the aggregate is also used to replay events.
     * @param block The block to execute if the aggregate is live
     */
    private fun react(block: () -> Unit) {
        if (AggregateLifecycle.isLive()) {
            block()
        }
    }

    private constructor()
}