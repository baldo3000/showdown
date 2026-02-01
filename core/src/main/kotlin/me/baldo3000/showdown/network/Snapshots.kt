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
    val hp: Float
) {
    companion object {
        fun fromEntity(entity: Entity): PlayerSnapshot {
            val id = entity[IdComponent.mapper]?.id
                ?: throw IllegalArgumentException("Entity must have an IdComponent. Entity: $entity")
            val position = entity[TransformComponent.mapper]?.position?.let { Vector2D(it.x, it.y) }
                ?: throw IllegalArgumentException("Entity must have a TransformComponent. Entity: $entity")
            val speed = entity[MoveComponent.mapper]?.speed?.let { Vector2D(it.x, it.y) }
                ?: throw IllegalArgumentException("Entity must have a MoveComponent. Entity: $entity")
            val hp = entity[PlayerComponent.mapper]?.life
                ?: throw IllegalArgumentException("Entity must have a PlayerComponent. Entity: $entity")
            return PlayerSnapshot(id, position, speed, /*direction,*/ hp)
        }
    }
}

@Serializable
data class BulletSnapshot(
    val id: Uuid,
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
            return BulletSnapshot(id, position, speed)
        }
    }
}

@Serializable
data class WorldSnapshot(
    val players: List<PlayerSnapshot>,
    val sequenceNumber: Int = 0
    //val bullets: List<BulletSnapshot>
) {
    companion object {
        fun fromEntities(entities: List<Entity>, sequenceNumber: Int): WorldSnapshot = WorldSnapshot(
            players = entities.filter {
                it[RemoveComponent.mapper] == null &&
                    it[IdComponent.mapper] != null &&
                    it[PlayerComponent.mapper] != null &&
                    it[TransformComponent.mapper] != null &&
                    it[MoveComponent.mapper] != null
            }.map { PlayerSnapshot.fromEntity(it) },
            sequenceNumber = sequenceNumber
            /*,
            bullets = entities.filter {
                it[RemoveComponent.mapper] == null &&
                    it[IdComponent.mapper] != null &&
                    it[TransformComponent.mapper] != null &&
                    it[MoveComponent.mapper] != null
            }.map { BulletSnapshot(it) }*/
        )
    }
}
