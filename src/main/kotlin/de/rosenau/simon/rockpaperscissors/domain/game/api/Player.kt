package de.rosenau.simon.rockpaperscissors.domain.game.api

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.annotation.JsonSubTypes
import com.fasterxml.jackson.annotation.JsonTypeInfo
import de.rosenau.simon.rockpaperscissors.domain.user.query.model.User
import java.util.*

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "type")
@JsonSubTypes(
    JsonSubTypes.Type(value = Player.Computer::class, name = "computer"),
    JsonSubTypes.Type(value = Player.User::class, name = "user")
)
sealed class Player {
    @JsonProperty("type")
    val type: String = this::class.simpleName!!.lowercase()

    abstract var ready: Boolean
    abstract var score: Int
    abstract var hand: Hand?

    data class Computer @JsonCreator constructor(
        @JsonProperty("name") val name: String,
        @JsonProperty("ready") override var ready: Boolean = false,
        @JsonProperty("score") override var score: Int = 0,
        @JsonProperty("selection") override var hand: Hand? = null
    ): Player()

    data class User @JsonCreator constructor(
        @JsonProperty("userId") val userId: UUID,
        @JsonProperty("ready") override var ready: Boolean = false,
        @JsonProperty("score") override var score: Int = 0,
        @JsonProperty("selection") override var hand: Hand? = null
    ): Player()

    fun matches(other: Id): Boolean {
        return when (this) {
            is Computer -> other is Id.Computer
            is User -> other is Id.User && this.userId == other.userId
        }
    }

    fun toId(): Id {
        return when (this) {
            is Computer -> Id.Computer(name)
            is User -> Id.User(userId)
        }
    }

    fun copy(ready: Boolean = this.ready, score: Int = this.score, hand: Hand? = this.hand): Player {
        return when (this) {
            is Computer -> Computer(name, ready, score, hand)
            is User -> User(userId, ready, score, hand)
        }
    }

    @JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "type")
    @JsonSubTypes(
        JsonSubTypes.Type(value = Id.Computer::class, name = "computer"),
        JsonSubTypes.Type(value = Id.User::class, name = "user")
    )
    sealed class Id {
        @JsonProperty("type")
        val type: String = this::class.simpleName!!.lowercase()

        data class Computer @JsonCreator constructor(@JsonProperty("name") val name: String): Id() {
            companion object {
                fun random(): Computer {
                    return Computer(computerNames.random())
                }
            }
        }

        data class User @JsonCreator constructor(@JsonProperty("userId") val userId: UUID): Id()

        fun toNewPlayer(): Player {
            return when (this) {
                is Computer -> Player.Computer(name)
                is User -> Player.User(userId)
            }
        }
    }
}

val User.playerId: Player.Id.User
    get() = Player.Id.User(id)

private val computerNames = listOf(
    "R2-D2",
    "C-3PO",
    "T-800",
    "Wall-E",
    "Data",
    "RoboCop",
    "HAL 9000",
    "Optimus Prime",
    "Baymax",
    "Johnny 5",
    "The Iron Giant"
)