package de.rosenau.simon.rockpaperscissors.util.jpa

import jakarta.persistence.*
import java.util.*

@MappedSuperclass
abstract class EntityBase(@Id val id: UUID) {

    @Column(name = "created_at", updatable = false)
    var createdAt: Date = Date()

    @Column(name = "last_updated_at")
    var lastUpdatedAt: Date = Date()

    @PrePersist
    fun prePersist() {
        createdAt = Date()
        lastUpdatedAt = Date()
    }

    @PreUpdate
    fun preUpdate() {
        lastUpdatedAt = Date()
    }
}