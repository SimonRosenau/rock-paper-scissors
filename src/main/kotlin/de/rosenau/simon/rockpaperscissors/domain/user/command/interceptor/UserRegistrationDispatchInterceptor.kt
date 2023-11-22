package de.rosenau.simon.rockpaperscissors.domain.user.command.interceptor

import de.rosenau.simon.rockpaperscissors.domain.user.api.command.RegisterUserCommand
import de.rosenau.simon.rockpaperscissors.domain.user.api.exception.UserNameAlreadyExistsException
import de.rosenau.simon.rockpaperscissors.domain.user.command.persistence.UserNameRepository
import jakarta.annotation.PostConstruct
import org.axonframework.commandhandling.CommandBus
import org.axonframework.commandhandling.CommandMessage
import org.axonframework.messaging.MessageDispatchInterceptor
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import java.util.function.BiFunction

@Component
class UserRegistrationDispatchInterceptor: MessageDispatchInterceptor<CommandMessage<*>> {
    @Autowired
    private lateinit var userNameRepository: UserNameRepository

    @Autowired
    private lateinit var commandBus: CommandBus

    @PostConstruct
    fun init() {
        commandBus.registerDispatchInterceptor(this)
    }

    override fun handle(messages: MutableList<out CommandMessage<*>>): BiFunction<Int, CommandMessage<*>, CommandMessage<*>> {
        return BiFunction { _, commandMessage ->
            if (commandMessage.payloadType == RegisterUserCommand::class.java) {
                val registerUserCommand = commandMessage.payload as RegisterUserCommand
                if (userNameRepository.existsByUsername(registerUserCommand.username.lowercase())) {
                    throw UserNameAlreadyExistsException(registerUserCommand.username)
                }
            }
            commandMessage
        }
    }
}