package de.rosenau.simon.rockpaperscissors.domain.user.query.model

import com.fasterxml.jackson.annotation.JsonIgnore
import de.rosenau.simon.rockpaperscissors.util.jpa.EntityBase
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Index
import jakarta.persistence.Table
import java.util.*

@Entity
@Table(name = "users", indexes = [
    Index(name = "idx_username", columnList = "username", unique = true)
])
class User(
    id: UUID,
    @Column(name = "username")
    val username: String,
    @Column(name = "password")
    @JsonIgnore val password: String,
): EntityBase(id)
