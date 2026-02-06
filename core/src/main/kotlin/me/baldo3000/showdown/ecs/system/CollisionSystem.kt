package me.baldo3000.showdown.ecs.system

import com.badlogic.ashley.core.Entity
import com.badlogic.ashley.systems.IteratingSystem
import com.badlogic.gdx.math.Circle
import com.badlogic.gdx.math.Intersector
import com.badlogic.gdx.math.Rectangle
import com.badlogic.gdx.math.Shape2D
import ktx.ashley.allOf
import ktx.ashley.exclude
import ktx.ashley.get
import ktx.log.logger
import me.baldo3000.showdown.ecs.component.*

class CollisionSystem :
    IteratingSystem(
        allOf(TransformComponent::class, MoveComponent::class, ColliderComponent::class)
            .exclude(RemoveComponent::class).get()
    ) {

    val entityCache: MutableSet<Entity> = mutableSetOf()

    override fun update(deltaTime: Float) {
        entityCache.clear()
        entityCache.addAll(entities)
        super.update(deltaTime)
    }

    override fun processEntity(entity: Entity, deltaTime: Float) {
        val transform = entity[TransformComponent.mapper]
        require(transform != null) { "Entity must have a TransformComponent. Entity: $entity" }
        val move = entity[MoveComponent.mapper]
        require(move != null) { "Entity must have a MoveComponent. Entity: $entity" }
        val collider = entity[ColliderComponent.mapper]
        require(collider != null) { "Entity must have a ColliderComponent. Entity: $entity" }
        val health = entity[HealthComponent.mapper]
        val id = entity[IdComponent.mapper]

        val iterator = entityCache.iterator()
        while (iterator.hasNext()) {
            val other = iterator.next()
            if (other != entity) {
                val otherTransform = other[TransformComponent.mapper]
                require(otherTransform != null) { "Entity must have a TransformComponent. Entity: $other" }
                val otherMove = other[MoveComponent.mapper]
                require(otherMove != null) { "Entity must have a MoveComponent. Entity: $other" }
                val otherCollider = other[ColliderComponent.mapper]
                require(otherCollider != null) { "Entity must have a ColliderComponent. Entity: $other" }
                val otherDamage = other[DamageComponent.mapper]

                if (id?.id != otherDamage?.sourceId && health != null && otherDamage != null
                    && collider.collider.overlaps(otherCollider.collider)
                ) {
                    health.health -= otherDamage.damage
                    other.add(RemoveComponent())
                    iterator.remove()
                    if (health.health <= 0f) {
                        entity.add(RemoveComponent())
                    }
                }
            }
        }
    }

    companion object {
        private val log = logger<CollisionSystem>()
    }
}

fun Shape2D.setCenter(x: Float, y: Float) {
    when (this) {
        is Circle -> {
            this.setPosition(x, y)
        }

        is Rectangle -> {
            this.setCenter(x, y)
        }

        else -> throw UnsupportedOperationException("Unsupported shape type: ${this::class}")
    }
}

fun Shape2D.overlaps(other: Shape2D): Boolean {
    return when (this) {
        is Circle -> when (other) {
            is Circle -> this.overlaps(other)
            is Rectangle -> Intersector.overlaps(this, other)
            else -> throw UnsupportedOperationException("Unsupported shape type: ${other::class}")
        }

        is Rectangle -> when (other) {
            is Circle -> Intersector.overlaps(other, this)
            is Rectangle -> this.overlaps(other)
            else -> throw UnsupportedOperationException("Unsupported shape type: ${other::class}")
        }

        else -> throw UnsupportedOperationException("Unsupported shape type: ${this::class}")
    }
}
