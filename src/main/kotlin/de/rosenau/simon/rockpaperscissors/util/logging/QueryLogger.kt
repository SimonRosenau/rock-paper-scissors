package de.rosenau.simon.rockpaperscissors.util.logging

import jakarta.annotation.PostConstruct
import org.axonframework.messaging.MessageDispatchInterceptor
import org.axonframework.queryhandling.QueryBus
import org.axonframework.queryhandling.QueryMessage
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import java.util.function.BiFunction

@Component
class QueryLogger: MessageDispatchInterceptor<QueryMessage<*, *>> {

    private val logger = LoggerFactory.getLogger(QueryLogger::class.java)

    @Autowired
    private lateinit var queryBus: QueryBus

    @PostConstruct
    fun init() {
        queryBus.registerDispatchInterceptor(this)
    }

    override fun handle(messages: MutableList<out QueryMessage<*, *>>): BiFunction<Int, QueryMessage<*, *>, QueryMessage<*, *>> {
        return BiFunction { _, eventMessage ->
            logger.debug("Query dispatched: {} - {}", eventMessage.payloadType.simpleName, eventMessage.payload)
            eventMessage
        }
    }
}