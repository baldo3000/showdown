package me.baldo3000.showdown.network

import com.badlogic.ashley.core.Entity
import kotlinx.serialization.Serializable
import ktx.ashley.get
import me.baldo3000.showdown.ecs.component.*
import kotlin.uuid.Uuid

@Serializable
data class Vector2D(
    val x: Float,
    val y: Float
)

@Serializable
data class PlayerSnapshot(
    val id: Uuid,
    val position: Vector2D,
    val speed: Vector2D,
    val health: Float
) {
    companion object {
        fun fromEntity(entity: Entity): PlayerSnapshot {
            val id = entity[IdComponent.mapper]?.id
                ?: throw IllegalArgumentException("Entity must have an IdComponent. Entity: $entity")
            val position = entity[TransformComponent.mapper]?.position?.let { Vector2D(it.x, it.y) }
                ?: throw IllegalArgumentException("Entity must have a TransformComponent. Entity: $entity")
            val speed = entity[MoveComponent.mapper]?.speed?.let { Vector2D(it.x, it.y) }
                ?: throw IllegalArgumentException("Entity must have a MoveComponent. Entity: $entity")
            val health = entity[HealthComponent.mapper]?.health
                ?: throw IllegalArgumentException("Entity must have a HealthComponent. Entity: $entity")
            return PlayerSnapshot(id, position, speed, health)
        }
    }
}

@Serializable
data class BulletSnapshot(
    val id: Uuid,
    val sourceId: Uuid?,
    val damage: Float,
    val position: Vector2D,
    val speed: Vector2D
) {
    companion object {
        fun fromEntity(entity: Entity): BulletSnapshot {
            val id = entity[IdComponent.mapper]?.id
                ?: throw IllegalArgumentException("Entity must have an IdComponent. Entity: $entity")
            val position = entity[TransformComponent.mapper]?.position?.let { Vector2D(it.x, it.y) }
                ?: throw IllegalArgumentException("Entity must have a TransformComponent. Entity: $entity")
            val speed = entity[MoveComponent.mapper]?.speed?.let { Vector2D(it.x, it.y) }
                ?: throw IllegalArgumentException("Entity must have a MoveComponent. Entity: $entity")
            val damage = entity[DamageComponent.mapper]
                ?: throw IllegalArgumentException("Entity must have a DamageComponent. Entity: $entity")
            return BulletSnapshot(id, damage.sourceId, damage.damage, position, speed)
        }
    }
}

@Serializable
data class WorldSnapshot(
    val players: List<PlayerSnapshot>,
    val bullets: List<BulletSnapshot>,
    val sequenceNumber: Int = 0
) {
    companion object {
        fun fromEntities(entities: List<Entity>, sequenceNumber: Int): WorldSnapshot = WorldSnapshot(
            sequenceNumber = sequenceNumber,
            players = entities.filter {
                it[RemoveComponent.mapper] == null &&
                    it[IdComponent.mapper] != null &&
                    it[HealthComponent.mapper] != null &&
                    it[TransformComponent.mapper] != null &&
                    it[MoveComponent.mapper] != null
            }.map { PlayerSnapshot.fromEntity(it) },
            bullets = entities.filter {
                it[RemoveComponent.mapper] == null &&
                    it[IdComponent.mapper] != null &&
                    it[DamageComponent.mapper] != null &&
                    it[TransformComponent.mapper] != null &&
                    it[MoveComponent.mapper] != null
            }.map { BulletSnapshot.fromEntity(it) }
        )
    }
}
