package me.baldo3000.showdown.ecs.system

import com.badlogic.ashley.core.Entity
import com.badlogic.ashley.systems.IteratingSystem
import com.badlogic.gdx.math.MathUtils
import ktx.ashley.allOf
import ktx.ashley.exclude
import ktx.ashley.get
import ktx.log.logger
import me.baldo3000.showdown.ecs.component.ColliderComponent
import me.baldo3000.showdown.ecs.component.MoveComponent
import me.baldo3000.showdown.ecs.component.RemoveComponent
import me.baldo3000.showdown.ecs.component.TransformComponent

private const val UPDATE_RATE = 1 / 60f

class MoveSystem :
    IteratingSystem(allOf(TransformComponent::class, MoveComponent::class).exclude(RemoveComponent::class).get()) {
    private var accumulator = 0f

    override fun setProcessing(processing: Boolean) {
        super.setProcessing(processing)
        //if (processing) accumulator = 0f
    }

    override fun update(deltaTime: Float) {
        accumulator += deltaTime
        while (accumulator >= UPDATE_RATE) {
            accumulator -= UPDATE_RATE

            // Update previous position
            entities.forEach { entity ->
                entity[TransformComponent.mapper]?.let { transform ->
                    transform.previousPosition.set(transform.position)
                }
            }

            super.update(UPDATE_RATE)
        }

        val alpha = accumulator / UPDATE_RATE
        // Update interpolation position
        entities.forEach { entity ->
            entity[TransformComponent.mapper]?.let { transform ->
                transform.interpolatedPosition.set(
                    MathUtils.lerp(transform.previousPosition.x, transform.position.x, alpha),
                    MathUtils.lerp(transform.previousPosition.y, transform.position.y, alpha),
                    transform.position.z
                )
            }
        }
    }

    override fun processEntity(entity: Entity, deltaTime: Float) {
        val transform = entity[TransformComponent.mapper]
        require(transform != null) { "Entity must have a TransformComponent. Entity: $entity" }
        val move = entity[MoveComponent.mapper]
        require(move != null) { "Entity must have a MoveComponent. Entity: $entity" }
        val collider = entity[ColliderComponent.mapper]

        // log.debug { "Moving entity $entity with speed ${move.speed}" }
        moveEntity(transform, move, collider, deltaTime)
    }

    private fun moveEntity(
        transform: TransformComponent,
        move: MoveComponent,
        collider: ColliderComponent?,
        deltaTime: Float
    ) {
        //transform.position.x = MathUtils.clamp()
        val deltaX = move.speed.x * deltaTime
        val deltaY = move.speed.y * deltaTime
        transform.position.x += deltaX
        transform.position.y += deltaY

        collider?.collider?.setCenter(transform.position.x, transform.position.y)
    }

    companion object {
        private val log = logger<MoveSystem>()
    }
}
