package me.baldo3000.showdown.network

import com.badlogic.ashley.core.Entity
import kotlinx.serialization.Serializable
import ktx.ashley.get
import me.baldo3000.showdown.data.Vector2D
import me.baldo3000.showdown.ecs.component.*
import kotlin.uuid.Uuid

@Serializable
data class PlayerSnapshot(
    val id: Uuid,
    val health: Float,
    val position: Vector2D,
    val speed: Vector2D
) {
    companion object {
        fun fromEntity(entity: Entity): PlayerSnapshot {
            val id = entity[IdComponent.mapper]?.id
                ?: throw IllegalArgumentException("Entity must have an IdComponent. Entity: $entity")
            val position = entity[TransformComponent.mapper]?.position?.to2D()
                ?: throw IllegalArgumentException("Entity must have a TransformComponent. Entity: $entity")
            val speed = entity[MoveComponent.mapper]?.speed?.let { Vector2D(it.x, it.y) }
                ?: throw IllegalArgumentException("Entity must have a MoveComponent. Entity: $entity")
            val health = entity[HealthComponent.mapper]?.health
                ?: throw IllegalArgumentException("Entity must have a HealthComponent. Entity: $entity")
            return PlayerSnapshot(id, health, position, speed)
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
            val position = entity[TransformComponent.mapper]?.position?.to2D()
                ?: throw IllegalArgumentException("Entity must have a TransformComponent. Entity: $entity")
            val speed = entity[MoveComponent.mapper]?.speed
                ?: throw IllegalArgumentException("Entity must have a MoveComponent. Entity: $entity")
            val damage = entity[DamageComponent.mapper]
                ?: throw IllegalArgumentException("Entity must have a DamageComponent. Entity: $entity")
            return BulletSnapshot(id, damage.sourceId, damage.damage, position, speed)
        }
    }
}

@Serializable
data class WallSnapshot(
    val id: Uuid,
    val position: Vector2D,
    val size: Vector2D
) {
    companion object {
        fun fromEntity(entity: Entity): WallSnapshot {
            val id = entity[IdComponent.mapper]?.id
                ?: throw IllegalArgumentException("Entity must have an IdComponent. Entity: $entity")
            val position = entity[TransformComponent.mapper]
                ?: throw IllegalArgumentException("Entity must have a TransformComponent. Entity: $entity")
            return WallSnapshot(id, position.position.to2D(), position.size)
        }
    }
}


@Serializable
data class WorldSnapshot(
    val players: List<PlayerSnapshot>,
    val bullets: List<BulletSnapshot>,
    val walls: List<WallSnapshot>,
    val sequenceNumber: Int = 0
) {
    companion object {
        fun fromEntities(
            players: List<Entity>,
            bullets: List<Entity>,
            walls: List<Entity>,
            sequenceNumber: Int
        ): WorldSnapshot =
            WorldSnapshot(
                players.map { PlayerSnapshot.fromEntity(it) },
                bullets.map { BulletSnapshot.fromEntity(it) },
                walls.map { WallSnapshot.fromEntity(it) },
                sequenceNumber
            )
    }
}
