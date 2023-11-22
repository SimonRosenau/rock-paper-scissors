package de.rosenau.simon.rockpaperscissors.util.domain.game.command.saga

import de.rosenau.simon.rockpaperscissors.domain.game.api.Player
import de.rosenau.simon.rockpaperscissors.domain.game.api.command.JoinGameCommand
import de.rosenau.simon.rockpaperscissors.domain.game.api.command.MarkPlayerReadyCommand
import de.rosenau.simon.rockpaperscissors.domain.game.api.command.SelectHandCommand
import de.rosenau.simon.rockpaperscissors.domain.game.api.event.GameIntendedEvent
import de.rosenau.simon.rockpaperscissors.domain.game.api.event.GameJoinedEvent
import de.rosenau.simon.rockpaperscissors.domain.game.api.event.GamePlayerReadyEvent
import de.rosenau.simon.rockpaperscissors.domain.game.api.event.GameRoundStartedEvent
import de.rosenau.simon.rockpaperscissors.domain.game.command.sagas.ComputerGameSaga
import org.axonframework.commandhandling.CommandMessage
import org.axonframework.test.matchers.Matchers
import org.axonframework.test.saga.FixtureConfiguration
import org.axonframework.test.saga.SagaTestFixture
import org.hamcrest.Matcher
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.time.Duration
import java.util.*
import java.util.function.Function

class ComputerGameSagaTest {
    private lateinit var fixture: FixtureConfiguration

    @BeforeEach
    fun setUp() {
        fixture = SagaTestFixture(ComputerGameSaga::class.java)
    }

    @Test
    fun `ensure computer joins game when game is intended for computer`() {
        val gameId = UUID.randomUUID()
        fixture.givenAggregate(gameId.toString())
            .published(GameIntendedEvent(gameId, Player.Id.User(UUID.randomUUID()), true))
            .whenTimeElapses(Duration.ofSeconds(10))
            .expectDispatchedCommandsMatching(commandMatcher<JoinGameCommand> {
                it.gameId == gameId && it.playerId is Player.Id.Computer
            })
    }

    @Test
    fun `ensure computer ends saga when game is not intended for computer`() {
        val gameId = UUID.randomUUID()
        fixture.givenAggregate(gameId.toString()).published()
            .whenAggregate(gameId.toString())
            .publishes(GameIntendedEvent(gameId, Player.Id.User(UUID.randomUUID()), false))
            .expectNoDispatchedCommands()
            .expectActiveSagas(0)
    }

    @Test
    fun `marks itself ready after joining`() {
        val gameId = UUID.randomUUID()
        fixture.givenAggregate(gameId.toString())
            .published(GameIntendedEvent(gameId, Player.Id.User(UUID.randomUUID()), true))
            .whenTimeElapses(Duration.ofSeconds(10))
            .expectDispatchedCommandsMatching(commandMatcher<JoinGameCommand> {
                it.gameId == gameId && it.playerId is Player.Id.Computer
            })

        fixture.givenAggregate(gameId.toString()).published(GameJoinedEvent(gameId, Player.Id.Computer.random()))
            .whenTimeElapses(Duration.ofSeconds(10))
            .expectDispatchedCommandsMatching(commandMatcher<MarkPlayerReadyCommand> {
                it.gameId == gameId && it.playerId is Player.Id.Computer
            })
    }

    @Test
    fun `selects its hand after the game round starts`() {
        val gameId = UUID.randomUUID()
        val userId = UUID.randomUUID()
        fixture.givenAggregate(gameId.toString()).published(GameIntendedEvent(gameId, Player.Id.User(userId), true))
            .whenTimeElapses(Duration.ofSeconds(10))
            .expectDispatchedCommandsMatching(commandMatcher<JoinGameCommand> {
                it.gameId == gameId && it.playerId is Player.Id.Computer
            })

        fixture.givenAggregate(gameId.toString()).published(
            GameJoinedEvent(gameId, Player.Id.Computer.random()),
            GamePlayerReadyEvent(gameId, Player.Id.User(userId)),
        )
            .whenTimeElapses(Duration.ofSeconds(10))
            .expectDispatchedCommandsMatching(commandMatcher<MarkPlayerReadyCommand> {
                it.gameId == gameId && it.playerId is Player.Id.Computer
            })

        fixture.givenAggregate(gameId.toString()).published(GameRoundStartedEvent(gameId))
            .whenTimeElapses(Duration.ofSeconds(10))
            .expectDispatchedCommandsMatching(commandMatcher<SelectHandCommand> {
                it.gameId == gameId && it.playerId is Player.Id.Computer
            })
    }

    /**
     * Helper function to match a command based on its type and a predicate.
     * @param predicate The predicate to match the command against.
     * @return The matcher to use in the test.
     */
    private inline fun <reified T> commandMatcher(predicate: Function<T, Boolean>): Matcher<out MutableList<in CommandMessage<*>>> {
        return Matchers.matches { messages: List<*> ->
            val matched = messages.find { message ->
                message is CommandMessage<*>
                        && message.payload is T
                        && predicate.apply(message.payload as T)
            }
            matched != null
        }
    }
}