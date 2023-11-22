package de.rosenau.simon.rockpaperscissors.util.helpers

import org.axonframework.commandhandling.CommandMessage
import org.axonframework.commandhandling.gateway.CommandGateway
import org.axonframework.queryhandling.QueryGateway
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Controller
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import java.time.Duration

@Controller
abstract class ControllerBase {

    @Autowired
    protected lateinit var commandGateway: CommandGateway

    @Autowired
    protected lateinit var queryGateway: QueryGateway

    fun <R, C, Q: Any> sendAndReturnUpdate(command: CommandMessage<C>, query: Q, responseType: Class<R>): Mono<R> {
        val result = queryGateway.subscriptionQuery(query, Void::class.java, responseType)
        return Mono.`when`(result.initialResult())
            .then(Mono.fromCompletionStage<C> { commandGateway.send(command) })
            .thenMany(result.updates())
            .timeout(Duration.ofSeconds(5))
            .next()
            .doFinally { result.cancel() }
    }

    fun <R, Q: Any> subscribe(query: Q, responseType: Class<R>): Flux<R> {
        val result = queryGateway.subscriptionQuery(query, responseType, responseType)
        return result.initialResult()
            .concatWith(result.updates())
            .doFinally { result.cancel() }
    }
}