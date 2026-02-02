package me.baldo3000.showdown.ecs.system

import com.badlogic.ashley.core.Entity
import com.badlogic.ashley.systems.IteratingSystem
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

        val toRemove = mutableSetOf<Entity>()

        for (other in entityCache) {
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
                    toRemove += other
                    if (health.health <= 0f) {
                        entity.add(RemoveComponent())
                    }
                }
            }
        }

        entityCache -= toRemove
    }

    companion object {
        private val log = logger<CollisionSystem>()
    }
}
