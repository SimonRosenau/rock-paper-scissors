package de.rosenau.simon.rockpaperscissors.config

import de.rosenau.simon.rockpaperscissors.domain.user.command.handler.UserEventHandler
import org.axonframework.common.jdbc.PersistenceExceptionResolver
import org.axonframework.common.jpa.EntityManagerProvider
import org.axonframework.common.transaction.TransactionManager
import org.axonframework.config.ConfigurerModule
import org.axonframework.eventhandling.EventBus
import org.axonframework.eventhandling.scheduling.EventScheduler
import org.axonframework.eventhandling.scheduling.java.SimpleEventScheduler
import org.axonframework.eventsourcing.eventstore.EventStorageEngine
import org.axonframework.eventsourcing.eventstore.jpa.JpaEventStorageEngine
import org.axonframework.serialization.Serializer
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.boot.autoconfigure.domain.EntityScan
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import java.util.concurrent.Executors

@Configuration
@EntityScan(
    "org.axonframework.eventsourcing.eventstore.jpa",
    "org.axonframework.eventhandling.tokenstore.jpa",
    "org.axonframework.modelling.saga.repository.jpa"
)
class AxonConfig {
    @Bean
    fun eventStorageEngine(
        serializer: Serializer,
        persistenceExceptionResolver: PersistenceExceptionResolver,
        @Qualifier("eventSerializer") eventSerializer: Serializer,
        entityManagerProvider: EntityManagerProvider,
        transactionManager: TransactionManager
    ): EventStorageEngine {
        return JpaEventStorageEngine.builder()
            .snapshotSerializer(serializer)
            .persistenceExceptionResolver(persistenceExceptionResolver)
            .eventSerializer(eventSerializer)
            .entityManagerProvider(entityManagerProvider)
            .transactionManager(transactionManager)
            .build()
    }

    @Bean
    fun subscribingProcessorsConfigurerModule(): ConfigurerModule {
        return ConfigurerModule { configurer ->
            configurer.eventProcessing { eventProcessingConfigurer ->
                eventProcessingConfigurer.registerSubscribingEventProcessor(UserEventHandler.PROCESSING_GROUP)
            }
        }
    }

    @Bean
    fun eventScheduler(eventBus: EventBus, transactionManager: TransactionManager): EventScheduler {
        return SimpleEventScheduler.builder()
            .eventBus(eventBus)
            .transactionManager(transactionManager)
            .scheduledExecutorService(Executors.newScheduledThreadPool(1))
            .build()
    }
}