package de.rosenau.simon.rockpaperscissors.util.logging

import jakarta.annotation.PostConstruct
import org.axonframework.commandhandling.CommandBus
import org.axonframework.commandhandling.CommandMessage
import org.axonframework.messaging.MessageDispatchInterceptor
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import java.util.function.BiFunction

@Component
class CommandLogger: MessageDispatchInterceptor<CommandMessage<*>> {

    private val logger = LoggerFactory.getLogger(CommandLogger::class.java)

    @Autowired
    private lateinit var commandBus: CommandBus

    @PostConstruct
    fun init() {
        commandBus.registerDispatchInterceptor(this)
    }

    override fun handle(messages: MutableList<out CommandMessage<*>>): BiFunction<Int, CommandMessage<*>, CommandMessage<*>> {
        return BiFunction { _, eventMessage ->
            logger.debug("Command dispatched: {} - {}", eventMessage.payloadType.simpleName, eventMessage.payload)
            eventMessage
        }
    }
}