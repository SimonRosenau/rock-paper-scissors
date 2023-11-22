package de.rosenau.simon.rockpaperscissors.domain.user.query

import de.rosenau.simon.rockpaperscissors.domain.user.api.event.UserRegisteredEvent
import de.rosenau.simon.rockpaperscissors.domain.user.api.exception.UserNotFoundException
import de.rosenau.simon.rockpaperscissors.domain.user.api.query.GetUserByCorrelationIdQuery
import de.rosenau.simon.rockpaperscissors.domain.user.api.query.GetUserByIdQuery
import de.rosenau.simon.rockpaperscissors.domain.user.api.query.GetUserByUsernameQuery
import de.rosenau.simon.rockpaperscissors.domain.user.query.model.User
import org.axonframework.eventhandling.EventHandler
import org.axonframework.messaging.annotation.MetaDataValue
import org.axonframework.queryhandling.QueryHandler
import org.axonframework.queryhandling.QueryUpdateEmitter
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import java.util.*

@Component
class UserProjection {
    @Autowired
    private lateinit var queryUpdateEmitter: QueryUpdateEmitter
    @Autowired
    private lateinit var userRepository: UserRepository

    // EventHandlers

    @EventHandler
    fun on(event: UserRegisteredEvent, @MetaDataValue("correlationId") correlationId: String) {
        val user = User(event.userId, event.username, event.password)
        userRepository.save(user)
        notifyUserUpdate(user, correlationId)
    }

    // QueryHandlers

    @QueryHandler
    fun handle(query: GetUserByCorrelationIdQuery): Optional<Void> {
        // Later resolved through the queryUpdateEmitter
        return Optional.empty()
    }

    @QueryHandler
    fun handle(query: GetUserByIdQuery): User {
        return userRepository.findById(query.userId)
            .orElseThrow { UserNotFoundException(query.userId.toString()) }
    }

    @QueryHandler
    fun handle(query: GetUserByUsernameQuery): User {
        return userRepository.findByUsername(query.username)
            .orElseThrow { UserNotFoundException(query.username) }
    }

    private fun notifyUserUpdate(user: User, correlationId: String) {
        queryUpdateEmitter.emit(GetUserByCorrelationIdQuery::class.java, { it.correlationId == correlationId }, user)
        queryUpdateEmitter.emit(GetUserByIdQuery::class.java, { it.userId == user.id }, user)
        queryUpdateEmitter.emit(GetUserByUsernameQuery::class.java, { it.username == user.username }, user)
    }
}