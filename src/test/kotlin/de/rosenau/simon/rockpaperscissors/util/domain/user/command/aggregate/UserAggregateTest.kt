package de.rosenau.simon.rockpaperscissors.util.domain.user.command.aggregate

import de.rosenau.simon.rockpaperscissors.domain.user.api.command.RegisterUserCommand
import de.rosenau.simon.rockpaperscissors.domain.user.api.event.UserRegisteredEvent
import de.rosenau.simon.rockpaperscissors.domain.user.command.aggregate.UserAggregate
import org.axonframework.test.aggregate.AggregateTestFixture
import org.axonframework.test.aggregate.FixtureConfiguration
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.util.*

class UserAggregateTest {
    private lateinit var fixture: FixtureConfiguration<UserAggregate>

    @BeforeEach
    fun setUp() {
        fixture = AggregateTestFixture(UserAggregate::class.java)
    }

    @Test
    fun `test register user`() {
        val userId = UUID.randomUUID()
        fixture.givenNoPriorActivity()
            .`when`(RegisterUserCommand(userId, "test", "password"))
            .expectSuccessfulHandlerExecution()
            .expectEvents(UserRegisteredEvent(userId, "test", "password"))
            .expectState { aggregate ->
                assert(aggregate.userId == userId)
                assert(aggregate.username == "test")
                assert(aggregate.password == "password")
            }
    }
}