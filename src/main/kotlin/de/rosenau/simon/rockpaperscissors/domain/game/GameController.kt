package de.rosenau.simon.rockpaperscissors.domain.game

import de.rosenau.simon.rockpaperscissors.config.security.UserPrincipal
import de.rosenau.simon.rockpaperscissors.domain.game.api.command.IntentGameCommand
import de.rosenau.simon.rockpaperscissors.domain.game.api.command.JoinGameCommand
import de.rosenau.simon.rockpaperscissors.domain.game.api.command.MarkPlayerReadyCommand
import de.rosenau.simon.rockpaperscissors.domain.game.api.command.SelectHandCommand
import de.rosenau.simon.rockpaperscissors.domain.game.api.dto.SelectHandDto
import de.rosenau.simon.rockpaperscissors.domain.game.api.playerId
import de.rosenau.simon.rockpaperscissors.domain.game.api.query.GetGameByCorrelationIdQuery
import de.rosenau.simon.rockpaperscissors.domain.game.api.query.GetGameByIdQuery
import de.rosenau.simon.rockpaperscissors.domain.game.query.model.Game
import de.rosenau.simon.rockpaperscissors.util.helpers.ControllerBase
import jakarta.validation.Valid
import org.axonframework.commandhandling.CommandMessage
import org.axonframework.commandhandling.GenericCommandMessage
import org.springframework.http.MediaType
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.*
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import java.util.*
import java.util.concurrent.CompletableFuture

@RestController
@RequestMapping("/v1/games")
class GameController: ControllerBase() {
    @PostMapping("/start")
    fun start(@AuthenticationPrincipal principal: UserPrincipal): Mono<Game> {
        return dispatch(IntentGameCommand(UUID.randomUUID(), principal.user.playerId, true))
    }

    @GetMapping("/{gameId}")
    fun get(@PathVariable gameId: UUID): CompletableFuture<Game> {
        return queryGateway.query(GetGameByIdQuery(gameId), Game::class.java)
    }

    @GetMapping("/{gameId}/stream", produces = [MediaType.TEXT_EVENT_STREAM_VALUE, MediaType.APPLICATION_NDJSON_VALUE])
    fun get(@AuthenticationPrincipal principal: UserPrincipal, @PathVariable gameId: UUID): Flux<Game> {
        return subscribe(GetGameByIdQuery(gameId), Game::class.java)
    }

    @PostMapping("/{gameId}/join")
    fun join(@AuthenticationPrincipal principal: UserPrincipal, @PathVariable gameId: UUID): Mono<Game> {
        return dispatch(JoinGameCommand(gameId, principal.user.playerId))
    }

    @PostMapping("/{gameId}/ready")
    fun ready(@AuthenticationPrincipal principal: UserPrincipal, @PathVariable gameId: UUID): Mono<Game> {
        return dispatch(MarkPlayerReadyCommand(gameId, principal.user.playerId))
    }

    @PostMapping("/{gameId}/hand")
    fun hand(
        @AuthenticationPrincipal principal: UserPrincipal,
        @PathVariable gameId: UUID,
        @Valid @RequestBody body: SelectHandDto.Request
    ): Mono<Game> {
        return dispatch(SelectHandCommand(gameId, principal.user.playerId, body.hand))
    }

    private fun <C: Any> dispatch(command: C): Mono<Game> {
        val message: CommandMessage<C> = GenericCommandMessage.asCommandMessage(command)
        val query = GetGameByCorrelationIdQuery(message.identifier)
        return sendAndReturnUpdate(message, query, Game::class.java)
    }
}