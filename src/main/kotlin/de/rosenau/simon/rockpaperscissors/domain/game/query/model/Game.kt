package de.rosenau.simon.rockpaperscissors.domain.game.query.model

import de.rosenau.simon.rockpaperscissors.domain.game.api.GameState
import de.rosenau.simon.rockpaperscissors.domain.game.api.Player
import de.rosenau.simon.rockpaperscissors.util.jpa.EntityBase
import jakarta.persistence.*
import org.hibernate.annotations.JdbcTypeCode
import org.hibernate.type.SqlTypes
import java.util.*

@Entity
@Table(name = "games")
class Game(
    id: UUID,
    @Column(name = "state")
    @Enumerated(EnumType.STRING)
    var state: GameState,
    /**
     * The data structure, event handling and domain logic already supports more than two players.
     * However, the UI does not (for now). Therefor the backend also initializes games with only two players and also always a computer opponent.
     */
    @Column(name = "players", columnDefinition = "jsonb")
    @JdbcTypeCode(SqlTypes.JSON)
    var players: MutableSet<Player>,
    @Column(name = "round")
    var round: Int,
): EntityBase(id) {

    /**
     * Updates the player with the given id by applying the given update function.
     * If no player with the given id exists, nothing happens.
     * @param playerId the id of the player to update
     * @param update the update function to apply
     * @return this game (for method chaining)
     */
    fun updatePlayer(playerId: Player.Id, update: (Player) -> Player): Game {
        players = players.map { if (it.matches(playerId)) update(it) else it }.toMutableSet()
        return this
    }

    /**
     * Updates all players by applying the given update function.
     * @param update the update function to apply
     * @return this game (for method chaining)
     */
    fun updatePlayers(update: (Player) -> Player): Game {
        players = players.map { update(it) }.toMutableSet()
        return this
    }
}