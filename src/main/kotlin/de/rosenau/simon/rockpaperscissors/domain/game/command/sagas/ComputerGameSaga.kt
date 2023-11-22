package de.rosenau.simon.rockpaperscissors.domain.game.command.sagas

import de.rosenau.simon.rockpaperscissors.domain.game.api.Hand
import de.rosenau.simon.rockpaperscissors.domain.game.api.Player
import de.rosenau.simon.rockpaperscissors.domain.game.api.command.JoinGameCommand
import de.rosenau.simon.rockpaperscissors.domain.game.api.command.MarkPlayerReadyCommand
import de.rosenau.simon.rockpaperscissors.domain.game.api.command.SelectHandCommand
import de.rosenau.simon.rockpaperscissors.domain.game.api.event.GameFinishedEvent
import de.rosenau.simon.rockpaperscissors.domain.game.api.event.GameIntendedEvent
import de.rosenau.simon.rockpaperscissors.domain.game.api.event.GameJoinedEvent
import de.rosenau.simon.rockpaperscissors.domain.game.api.event.GameRoundStartedEvent
import org.axonframework.commandhandling.gateway.CommandGateway
import org.axonframework.eventhandling.scheduling.EventScheduler
import org.axonframework.modelling.saga.SagaEventHandler
import org.axonframework.modelling.saga.SagaLifecycle
import org.axonframework.modelling.saga.StartSaga
import org.axonframework.spring.stereotype.Saga
import org.springframework.beans.factory.annotation.Autowired
import java.time.Duration
import java.util.*

@Saga
class ComputerGameSaga {
    @Autowired
    @Transient
    private lateinit var commandGateway: CommandGateway

    @Autowired
    @Transient
    private lateinit var eventScheduler: EventScheduler

    private val computerId = Player.Id.Computer.random()

    @StartSaga
    @SagaEventHandler(associationProperty = "gameId")
    fun on(event: GameIntendedEvent) {
        if (event.computer) {
            // Delay command execution by a few seconds
            eventScheduler.schedule(
                this.randomDelay(),
                DispatchDelayedComputerJoinEvent(event.gameId)
            )
        } else {
            SagaLifecycle.end()
        }
    }

    @SagaEventHandler(associationProperty = "gameId")
    fun on(event: DispatchDelayedComputerJoinEvent) {
        commandGateway.sendAndWait<Unit>(JoinGameCommand(event.gameId, computerId))
    }

    @SagaEventHandler(associationProperty = "gameId")
    fun on(event: GameJoinedEvent) {
        // Delay command execution by a few seconds
        eventScheduler.schedule(
            this.randomDelay(),
            DispatchDelayedComputerReadyEvent(event.gameId)
        )
    }

    @SagaEventHandler(associationProperty = "gameId")
    fun on(event: DispatchDelayedComputerReadyEvent) {
        commandGateway.sendAndWait<Unit>(MarkPlayerReadyCommand(event.gameId, computerId))
    }

    @SagaEventHandler(associationProperty = "gameId")
    fun on(event: GameRoundStartedEvent) {
        // Delay command execution by a few seconds
        eventScheduler.schedule(
            this.randomDelay().plus(Duration.ofSeconds(3)),
            DispatchDelayedComputerSelectHandEvent(event.gameId)
        )
    }

    @SagaEventHandler(associationProperty = "gameId")
    fun on(event: DispatchDelayedComputerSelectHandEvent) {
        commandGateway.sendAndWait<Unit>(SelectHandCommand(event.gameId, computerId, event.hand))
    }

    @SagaEventHandler(associationProperty = "gameId")
    fun on(event: GameFinishedEvent) {
        SagaLifecycle.end()
    }

    /**
     * Events to control scheduled command dispatching.
     */
    data class DispatchDelayedComputerJoinEvent(val gameId: UUID)
    data class DispatchDelayedComputerReadyEvent(val gameId: UUID)
    data class DispatchDelayedComputerSelectHandEvent(val gameId: UUID, val hand: Hand = Hand.values().random())

    /**
     * Returns a random delay between 1 and 3 seconds.
     */
    private fun randomDelay() = Duration.ofSeconds(1).multipliedBy((1..3).random().toLong())
}