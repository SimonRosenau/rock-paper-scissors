package de.rosenau.simon.rockpaperscissors.domain.user.command.handler

import de.rosenau.simon.rockpaperscissors.domain.user.api.event.UserRegisteredEvent
import de.rosenau.simon.rockpaperscissors.domain.user.command.persistence.UserNameJpaEntity
import de.rosenau.simon.rockpaperscissors.domain.user.command.persistence.UserNameRepository
import org.axonframework.config.ProcessingGroup
import org.axonframework.eventhandling.EventHandler
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

@Component
@ProcessingGroup(UserEventHandler.PROCESSING_GROUP)
class UserEventHandler {
    companion object {
        const val PROCESSING_GROUP = "user-name-entity-group"
    }

    @Autowired
    private lateinit var userNameRepository: UserNameRepository

    @EventHandler
    fun on(event: UserRegisteredEvent) {
        userNameRepository.save(UserNameJpaEntity(event.username.lowercase(), event.userId))
    }
}