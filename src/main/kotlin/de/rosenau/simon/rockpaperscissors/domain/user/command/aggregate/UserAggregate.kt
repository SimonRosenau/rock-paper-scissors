package de.rosenau.simon.rockpaperscissors.domain.user.command.aggregate

import de.rosenau.simon.rockpaperscissors.domain.user.api.command.RegisterUserCommand
import de.rosenau.simon.rockpaperscissors.domain.user.api.event.UserRegisteredEvent
import org.axonframework.commandhandling.CommandHandler
import org.axonframework.eventsourcing.EventSourcingHandler
import org.axonframework.modelling.command.AggregateIdentifier
import org.axonframework.modelling.command.AggregateLifecycle
import org.axonframework.spring.stereotype.Aggregate
import java.util.*

@Aggregate
class UserAggregate {
    @AggregateIdentifier
    lateinit var userId: UUID
    lateinit var username: String
    lateinit var password: String

    @CommandHandler
    constructor(command: RegisterUserCommand) {
        AggregateLifecycle.apply(UserRegisteredEvent(command.userId, command.username, command.password))
    }

    @EventSourcingHandler
    fun on(event: UserRegisteredEvent) {
        userId = event.userId
        username = event.username
        password = event.password
    }

    private constructor()
}
