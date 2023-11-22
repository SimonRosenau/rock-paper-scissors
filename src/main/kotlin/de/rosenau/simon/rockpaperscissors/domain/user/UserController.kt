package de.rosenau.simon.rockpaperscissors.domain.user

import de.rosenau.simon.rockpaperscissors.config.security.UserPrincipal
import de.rosenau.simon.rockpaperscissors.domain.user.api.command.RegisterUserCommand
import de.rosenau.simon.rockpaperscissors.domain.user.api.dto.CreateUserDto
import de.rosenau.simon.rockpaperscissors.domain.user.api.query.GetUserByCorrelationIdQuery
import de.rosenau.simon.rockpaperscissors.domain.user.api.query.GetUserByIdQuery
import de.rosenau.simon.rockpaperscissors.domain.user.query.model.User
import de.rosenau.simon.rockpaperscissors.util.Passwords
import de.rosenau.simon.rockpaperscissors.util.helpers.ControllerBase
import jakarta.validation.Valid
import org.axonframework.commandhandling.CommandMessage
import org.axonframework.commandhandling.GenericCommandMessage
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.*
import reactor.core.publisher.Mono
import java.util.*
import java.util.concurrent.CompletableFuture

@RestController
@RequestMapping("/v1/users")
class UserController: ControllerBase() {
    @PostMapping
    fun createUser(@Valid @RequestBody body: CreateUserDto): Mono<User> {
        return dispatch(RegisterUserCommand(UUID.randomUUID(), body.username, Passwords.hash(body.password)))
    }

    @GetMapping("/profile")
    fun retrieveProfile(@AuthenticationPrincipal principal: UserPrincipal): User {
        return principal.user
    }

    @GetMapping("/{userId}")
    fun retrieveUser(@PathVariable userId: UUID): CompletableFuture<User> {
        return queryGateway.query(GetUserByIdQuery(userId), User::class.java)
    }

    /**
     * Dispatches a command and returns the updated view model.
     * @param command The command to dispatch.
     * @return The updated view model.
     */
    private fun <C: Any> dispatch(command: C): Mono<User> {
        val message: CommandMessage<C> = GenericCommandMessage.asCommandMessage(command)
        val query = GetUserByCorrelationIdQuery(message.identifier)
        return sendAndReturnUpdate(message, query, User::class.java)
    }
}