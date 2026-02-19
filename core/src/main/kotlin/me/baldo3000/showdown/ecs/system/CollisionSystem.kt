package me.baldo3000.showdown.ecs.system

import com.badlogic.ashley.core.Entity
import com.badlogic.ashley.systems.IteratingSystem
import com.badlogic.gdx.math.Circle
import com.badlogic.gdx.math.Intersector
import com.badlogic.gdx.math.Rectangle
import com.badlogic.gdx.math.Shape2D
import ktx.ashley.*
import ktx.log.logger
import ktx.math.minusAssign
import ktx.math.times
import me.baldo3000.showdown.ecs.component.*
import me.baldo3000.showdown.ecs.component.event.CheckGameEndComponent

private const val UPDATE_RATE = 1 / 60f

class CollisionSystem :
    IteratingSystem(
        allOf(TransformComponent::class, MoveComponent::class, ColliderComponent::class)
            .exclude(RemoveComponent::class).get()
    ) {
    private var accumulator = 0f

    override fun setProcessing(processing: Boolean) {
        super.setProcessing(processing)
        if (processing) accumulator = 0f
    }

    override fun update(deltaTime: Float) {
        accumulator += deltaTime
        while (accumulator >= UPDATE_RATE) {
            accumulator -= UPDATE_RATE
            super.update(UPDATE_RATE)
        }
    }

    override fun processEntity(entity: Entity, deltaTime: Float) {
        val transform = entity[TransformComponent.mapper]
        require(transform != null) { "Entity must have a TransformComponent. Entity: $entity" }
        val move = entity[MoveComponent.mapper]
        require(move != null) { "Entity must have a MoveComponent. Entity: $entity" }
        val collider = entity[ColliderComponent.mapper]
        require(collider != null) { "Entity must have a ColliderComponent. Entity: $entity" }
        val damage = entity[DamageComponent.mapper]

        for (other in engine.getEntitiesFor(othersFamily)) {
            if (other != entity) {
                val otherCollider = other[ColliderComponent.mapper]
                require(otherCollider != null) { "Entity must have a ColliderComponent. Entity: $other" }

                if (collider.collider.overlaps(otherCollider.collider)) {
                    val otherId = other[IdComponent.mapper]
                    val otherHealth = other[HealthComponent.mapper]
                    val otherDamage = other[DamageComponent.mapper]

                    if (otherDamage == null) {
                        // Entity is player and other is not a bullet
                        if (damage == null) {
                            transform.position.minusAssign(move.speed * deltaTime)
                            collider.collider.setCenter(transform.position.x, transform.position.y)
                        }
                        // Entity is a bullet and other is not a bullet
                        else if (damage.sourceId != otherId?.id) {
                            entity.add(RemoveComponent())
                            otherHealth?.apply {
                                health -= damage.damage
                                if (health <= 0f) {
                                    engine.entity { with<CheckGameEndComponent>() }
                                    other.add(RemoveComponent())
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    companion object {
        private val log = logger<CollisionSystem>()
        private val othersFamily = allOf(TransformComponent::class, ColliderComponent::class)
            .exclude(RemoveComponent::class).get()
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
