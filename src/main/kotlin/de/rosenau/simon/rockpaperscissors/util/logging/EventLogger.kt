package de.rosenau.simon.rockpaperscissors.util.logging

import jakarta.annotation.PostConstruct
import org.axonframework.eventhandling.EventBus
import org.axonframework.eventhandling.EventMessage
import org.axonframework.messaging.MessageDispatchInterceptor
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import java.util.function.BiFunction

@Component
class EventLogger: MessageDispatchInterceptor<EventMessage<*>> {

    private val logger = LoggerFactory.getLogger(EventLogger::class.java)

    @Autowired
    private lateinit var eventBus: EventBus

    @PostConstruct
    fun init() {
        eventBus.registerDispatchInterceptor(this)
    }

    override fun handle(messages: MutableList<out EventMessage<*>>): BiFunction<Int, EventMessage<*>, EventMessage<*>> {
        return BiFunction { _, eventMessage ->
            logger.debug("Event dispatched: {} - {}", eventMessage.payloadType.simpleName, eventMessage.payload)
            eventMessage
        }
    }
}