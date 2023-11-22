package de.rosenau.simon.rockpaperscissors.util.domain.game.command.aggregate

import de.rosenau.simon.rockpaperscissors.domain.game.api.GameState
import de.rosenau.simon.rockpaperscissors.domain.game.api.Hand
import de.rosenau.simon.rockpaperscissors.domain.game.api.Player
import de.rosenau.simon.rockpaperscissors.domain.game.api.command.IntentGameCommand
import de.rosenau.simon.rockpaperscissors.domain.game.api.command.JoinGameCommand
import de.rosenau.simon.rockpaperscissors.domain.game.api.command.MarkPlayerReadyCommand
import de.rosenau.simon.rockpaperscissors.domain.game.api.command.SelectHandCommand
import de.rosenau.simon.rockpaperscissors.domain.game.api.event.*
import de.rosenau.simon.rockpaperscissors.domain.game.command.aggregate.GameAggregate
import de.rosenau.simon.rockpaperscissors.domain.user.api.exception.GameAlreadyFullException
import de.rosenau.simon.rockpaperscissors.domain.user.api.exception.GameNotStartedException
import de.rosenau.simon.rockpaperscissors.domain.user.api.exception.PlayerAlreadyJoinedException
import de.rosenau.simon.rockpaperscissors.domain.user.api.exception.PlayerAlreadySelectedHandException
import org.axonframework.test.aggregate.AggregateTestFixture
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.util.*

class GameAggregateTest {
    private lateinit var fixture: AggregateTestFixture<GameAggregate>

    @BeforeEach
    fun setUp() {
        fixture = AggregateTestFixture(GameAggregate::class.java)
    }

    @Test
    fun `ensure game creation works`() {
        val gameId = UUID.randomUUID()
        val userId = UUID.randomUUID()
        fixture.givenNoPriorActivity()
            .`when`(IntentGameCommand(gameId, Player.Id.User(userId), true))
            .expectSuccessfulHandlerExecution()
            .expectEvents(GameIntendedEvent(gameId, Player.Id.User(userId), true))
            .expectState { aggregate ->
                assert(aggregate.gameId == gameId)
                assert(aggregate.state == GameState.CREATED)
                assert(aggregate.players.size == 1)
                assert(aggregate.players.first().matches(Player.Id.User(userId)))
                // Check that the player has score 0, is not ready and has no hand
                assert(aggregate.players.first().score == 0)
                assert(!aggregate.players.first().ready)
                assert(aggregate.players.first().hand == null)
            }
    }

    @Test
    fun `ensure game join works`() {
        val gameId = UUID.randomUUID()
        val firstUserId = UUID.randomUUID()
        val secondUserId = UUID.randomUUID()
        fixture.given(GameIntendedEvent(gameId, Player.Id.User(firstUserId), false))
            .`when`(JoinGameCommand(gameId, Player.Id.User(secondUserId)))
            .expectSuccessfulHandlerExecution()
            .expectEvents(GameJoinedEvent(gameId, Player.Id.User(secondUserId)))
            .expectState { aggregate ->
                assert(aggregate.gameId == gameId)
                assert(aggregate.state == GameState.CREATED)
                assert(aggregate.players.size == 2)
                // Assert set contains both players
                assert(aggregate.players.any { it.matches(Player.Id.User(firstUserId)) })
                assert(aggregate.players.any { it.matches(Player.Id.User(secondUserId)) })
                // Assert all players have score 0, are not ready and have no hand
                assert(aggregate.players.all { it.score == 0 })
                assert(aggregate.players.all { !it.ready })
                assert(aggregate.players.all { it.hand == null })
            }
    }

    @Test
    fun `ensure game join throws error when game is already full`() {
        val gameId = UUID.randomUUID()
        fixture.given(GameIntendedEvent(gameId, Player.Id.User(UUID.randomUUID()), false))
            .andGiven(GameJoinedEvent(gameId, Player.Id.User(UUID.randomUUID())))
            .`when`(JoinGameCommand(gameId, Player.Id.User(UUID.randomUUID())))
            .expectException(GameAlreadyFullException::class.java)
    }

    @Test
    fun `ensure game join throws error when player has already joined`() {
        val gameId = UUID.randomUUID()
        val userId = UUID.randomUUID()
        fixture.given(GameIntendedEvent(gameId, Player.Id.User(userId), false))
            .`when`(JoinGameCommand(gameId, Player.Id.User(userId)))
            .expectException(PlayerAlreadyJoinedException::class.java)
    }

    @Test
    fun `ensure marking player as ready works`() {
        val gameId = UUID.randomUUID()
        val firstUserId = UUID.randomUUID()
        val secondUserId = UUID.randomUUID()
        fixture.given(GameIntendedEvent(gameId, Player.Id.User(firstUserId), false))
            .andGiven(GameJoinedEvent(gameId, Player.Id.User(secondUserId)))
            .`when`(MarkPlayerReadyCommand(gameId, Player.Id.User(secondUserId)))
            .expectSuccessfulHandlerExecution()
            .expectEvents(GamePlayerReadyEvent(gameId, Player.Id.User(secondUserId)))
            .expectState { aggregate ->
                // Ensure first player is still not ready
                assert(!aggregate.players.first { it.matches(Player.Id.User(firstUserId)) }.ready)
                // Ensure second player is ready
                assert(aggregate.players.first { it.matches(Player.Id.User(secondUserId)) }.ready)
            }
    }

    @Test
    fun `ensure game starts when both players are ready`() {
        val gameId = UUID.randomUUID()
        val firstUserId = UUID.randomUUID()
        val secondUserId = UUID.randomUUID()
        fixture.given(GameIntendedEvent(gameId, Player.Id.User(firstUserId), false))
            .andGiven(GameJoinedEvent(gameId, Player.Id.User(secondUserId)))
            .andGiven(GamePlayerReadyEvent(gameId, Player.Id.User(firstUserId)))
            .`when`(MarkPlayerReadyCommand(gameId, Player.Id.User(secondUserId)))
            .expectSuccessfulHandlerExecution()
            .expectEvents(
                GamePlayerReadyEvent(gameId, Player.Id.User(secondUserId)),
                GameRoundStartedEvent(gameId)
            )
            .expectState { aggregate ->
                // Ensure first player is ready
                assert(aggregate.players.first { it.matches(Player.Id.User(firstUserId)) }.ready)
                // Ensure second player is ready
                assert(aggregate.players.first { it.matches(Player.Id.User(secondUserId)) }.ready)
                // Ensure game is started
                assert(aggregate.state == GameState.STARTED)
            }
    }

    @Test
    fun `ensure player can select hand`() {
        val gameId = UUID.randomUUID()
        val firstUserId = UUID.randomUUID()
        val secondUserId = UUID.randomUUID()
        fixture.given(GameIntendedEvent(gameId, Player.Id.User(firstUserId), false))
            .andGiven(GameJoinedEvent(gameId, Player.Id.User(secondUserId)))
            .andGiven(GamePlayerReadyEvent(gameId, Player.Id.User(firstUserId)))
            .andGiven(GamePlayerReadyEvent(gameId, Player.Id.User(secondUserId)))
            .andGiven(GameRoundStartedEvent(gameId))
            .`when`(SelectHandCommand(gameId, Player.Id.User(firstUserId), Hand.ROCK))
            .expectSuccessfulHandlerExecution()
            .expectEvents(HandSelectedEvent(gameId, Player.Id.User(firstUserId), Hand.ROCK))
            .expectState { aggregate ->
                // Ensure first player has selected hand
                assert(aggregate.players.first { it.matches(Player.Id.User(firstUserId)) }.hand == Hand.ROCK)
                // Ensure second player has not selected hand
                assert(aggregate.players.first { it.matches(Player.Id.User(secondUserId)) }.hand == null)
            }
    }

    @Test
    fun `ensure player can only select the hand when the game is started`() {
        val gameId = UUID.randomUUID()
        val firstUserId = UUID.randomUUID()
        val secondUserId = UUID.randomUUID()
        fixture.given(GameIntendedEvent(gameId, Player.Id.User(firstUserId), false))
            .andGiven(GameJoinedEvent(gameId, Player.Id.User(secondUserId)))
            .andGiven(GamePlayerReadyEvent(gameId, Player.Id.User(firstUserId)))
            .andGiven(GamePlayerReadyEvent(gameId, Player.Id.User(secondUserId)))
            .`when`(SelectHandCommand(gameId, Player.Id.User(firstUserId), Hand.ROCK))
            .expectException(GameNotStartedException::class.java)
    }

    @Test
    fun `ensure player cannot change the hand`() {
        val gameId = UUID.randomUUID()
        val firstUserId = UUID.randomUUID()
        val secondUserId = UUID.randomUUID()
        fixture.given(GameIntendedEvent(gameId, Player.Id.User(firstUserId), false))
            .andGiven(GameJoinedEvent(gameId, Player.Id.User(secondUserId)))
            .andGiven(GamePlayerReadyEvent(gameId, Player.Id.User(firstUserId)))
            .andGiven(GamePlayerReadyEvent(gameId, Player.Id.User(secondUserId)))
            .andGiven(GameRoundStartedEvent(gameId))
            .andGiven(HandSelectedEvent(gameId, Player.Id.User(firstUserId), Hand.ROCK))
            .`when`(SelectHandCommand(gameId, Player.Id.User(firstUserId), Hand.PAPER))
            .expectException(PlayerAlreadySelectedHandException::class.java)
    }

    @Test
    fun `ensure player scores a point for beating the other player`() {
        val gameId = UUID.randomUUID()
        val firstUserId = UUID.randomUUID()
        val secondUserId = UUID.randomUUID()
        fixture.given(GameIntendedEvent(gameId, Player.Id.User(firstUserId), false))
            .andGiven(GameJoinedEvent(gameId, Player.Id.User(secondUserId)))
            .andGiven(GamePlayerReadyEvent(gameId, Player.Id.User(firstUserId)))
            .andGiven(GamePlayerReadyEvent(gameId, Player.Id.User(secondUserId)))
            .andGiven(GameRoundStartedEvent(gameId))
            .andGiven(HandSelectedEvent(gameId, Player.Id.User(firstUserId), Hand.ROCK))
            .`when`(SelectHandCommand(gameId, Player.Id.User(secondUserId), Hand.PAPER))
            .expectSuccessfulHandlerExecution()
            .expectEvents(
                HandSelectedEvent(gameId, Player.Id.User(secondUserId), Hand.PAPER),
                PlayerScoredEvent(gameId, Player.Id.User(secondUserId), 1),
                GameRoundStartedEvent(gameId)
            )
            .expectState { aggregate ->
                // Ensure first player has not scored a point
                assert(aggregate.players.first { it.matches(Player.Id.User(firstUserId)) }.score == 0)
                // Ensure second player has scored a point
                assert(aggregate.players.first { it.matches(Player.Id.User(secondUserId)) }.score == 1)
            }
    }

    @Test
    fun `ensure nobody scores a point when both select the same hand`() {
        val gameId = UUID.randomUUID()
        val firstUserId = UUID.randomUUID()
        val secondUserId = UUID.randomUUID()
        fixture.given(GameIntendedEvent(gameId, Player.Id.User(firstUserId), false))
            .andGiven(GameJoinedEvent(gameId, Player.Id.User(secondUserId)))
            .andGiven(GamePlayerReadyEvent(gameId, Player.Id.User(firstUserId)))
            .andGiven(GamePlayerReadyEvent(gameId, Player.Id.User(secondUserId)))
            .andGiven(GameRoundStartedEvent(gameId))
            .andGiven(HandSelectedEvent(gameId, Player.Id.User(firstUserId), Hand.ROCK))
            .`when`(SelectHandCommand(gameId, Player.Id.User(secondUserId), Hand.ROCK))
            .expectSuccessfulHandlerExecution()
            .expectEvents(
                HandSelectedEvent(gameId, Player.Id.User(secondUserId), Hand.ROCK),
                GameRoundStartedEvent(gameId)
            )
            .expectState { aggregate ->
                // Ensure first player has not scored a point
                assert(aggregate.players.first { it.matches(Player.Id.User(firstUserId)) }.score == 0)
                // Ensure second player has not scored a point
                assert(aggregate.players.first { it.matches(Player.Id.User(secondUserId)) }.score == 0)
            }
    }

    @Test
    fun `ensure game finishes when player scored the final point`() {
        val gameId = UUID.randomUUID()
        val firstUserId = UUID.randomUUID()
        val secondUserId = UUID.randomUUID()
        fixture.given(GameIntendedEvent(gameId, Player.Id.User(firstUserId), false))
            .andGiven(GameJoinedEvent(gameId, Player.Id.User(secondUserId)))
            .andGiven(GamePlayerReadyEvent(gameId, Player.Id.User(firstUserId)))
            .andGiven(GamePlayerReadyEvent(gameId, Player.Id.User(secondUserId)))
            .andGiven(GameRoundStartedEvent(gameId))
            .andGiven(HandSelectedEvent(gameId, Player.Id.User(firstUserId), Hand.ROCK))
            .andGiven(HandSelectedEvent(gameId, Player.Id.User(secondUserId), Hand.PAPER))
            .andGiven(PlayerScoredEvent(gameId, Player.Id.User(secondUserId), 1))
            .andGiven(GameRoundStartedEvent(gameId))
            .andGiven(HandSelectedEvent(gameId, Player.Id.User(firstUserId), Hand.ROCK))
            .andGiven(HandSelectedEvent(gameId, Player.Id.User(secondUserId), Hand.PAPER))
            .andGiven(PlayerScoredEvent(gameId, Player.Id.User(secondUserId), 1))
            .andGiven(GameRoundStartedEvent(gameId))
            .andGiven(HandSelectedEvent(gameId, Player.Id.User(firstUserId), Hand.ROCK))
            .`when`(SelectHandCommand(gameId, Player.Id.User(secondUserId), Hand.PAPER))
            .expectSuccessfulHandlerExecution()
            .expectEvents(
                HandSelectedEvent(gameId, Player.Id.User(secondUserId), Hand.PAPER),
                PlayerScoredEvent(gameId, Player.Id.User(secondUserId), 1),
                GameFinishedEvent(gameId)
            )
            .expectState { aggregate ->
                // Ensure first player has not scored a point
                assert(aggregate.players.first { it.matches(Player.Id.User(firstUserId)) }.score == 0)
                // Ensure second player has scored 3 points
                assert(aggregate.players.first { it.matches(Player.Id.User(secondUserId)) }.score == 3)
                // Ensure game is finished
                assert(aggregate.state == GameState.FINISHED)
            }
    }
}