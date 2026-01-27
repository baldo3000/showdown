package me.baldo3000.showdown.ecs.system

import com.badlogic.ashley.core.Entity
import com.badlogic.ashley.systems.IteratingSystem
import com.badlogic.gdx.math.MathUtils
import ktx.ashley.allOf
import ktx.ashley.exclude
import ktx.ashley.get
import ktx.log.logger
import me.baldo3000.showdown.ecs.component.ACCELERATION_FACTOR
import me.baldo3000.showdown.ecs.component.FacingComponent
import me.baldo3000.showdown.ecs.component.FacingDirection
import me.baldo3000.showdown.ecs.component.MAX_SPEED
import me.baldo3000.showdown.ecs.component.MoveComponent
import me.baldo3000.showdown.ecs.component.PlayerComponent
import me.baldo3000.showdown.ecs.component.RemoveComponent
import me.baldo3000.showdown.ecs.component.TransformComponent

private const val UPDATE_RATE = 1 / 60f

class MoveSystem : IteratingSystem(
    allOf(TransformComponent::class, MoveComponent::class).exclude(RemoveComponent::class).get()
) {
    private var accumulator = 0f

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

        val player = entity[PlayerComponent.mapper]
        if (player != null) {
            // Player movement
            entity[FacingComponent.mapper]?.let { facing ->
                movePlayer(transform, move, player, facing, deltaTime)
            }
        }
        // Other entities movement
        moveEntity(transform, move, deltaTime)
    }

    private fun movePlayer(
        transform: TransformComponent,
        move: MoveComponent,
        player: PlayerComponent,
        facing: FacingComponent,
        deltaTime: Float
    ) {
        move.speed.x = when (facing.direction) {
            FacingDirection.TOP_RIGHT -> move.speed.x + ACCELERATION_FACTOR * deltaTime / 1.4142f
            FacingDirection.RIGHT -> move.speed.x + ACCELERATION_FACTOR * deltaTime
            FacingDirection.BOTTOM_RIGHT -> move.speed.x + ACCELERATION_FACTOR * deltaTime / 1.4142f
            FacingDirection.BOTTOM_LEFT -> move.speed.x - ACCELERATION_FACTOR * deltaTime / 1.4142f
            FacingDirection.LEFT -> move.speed.x - ACCELERATION_FACTOR * deltaTime
            FacingDirection.TOP_LEFT -> move.speed.x - ACCELERATION_FACTOR * deltaTime / 1.4142f
            else -> 0f
        }

        move.speed.x = MathUtils.clamp(move.speed.x, -MAX_SPEED, MAX_SPEED)

        move.speed.y = when (facing.direction) {
            FacingDirection.TOP -> move.speed.y + ACCELERATION_FACTOR * deltaTime
            FacingDirection.TOP_RIGHT -> move.speed.y + ACCELERATION_FACTOR * deltaTime / 1.4142f
            FacingDirection.BOTTOM_RIGHT -> move.speed.y - ACCELERATION_FACTOR * deltaTime / 1.4142f
            FacingDirection.BOTTOM -> move.speed.y - ACCELERATION_FACTOR * deltaTime
            FacingDirection.BOTTOM_LEFT -> move.speed.y - ACCELERATION_FACTOR * deltaTime / 1.4142f
            FacingDirection.TOP_LEFT -> move.speed.y + ACCELERATION_FACTOR * deltaTime / 1.4142f
            else -> 0f
        }

        move.speed.y = MathUtils.clamp(move.speed.y, -MAX_SPEED, MAX_SPEED)
    }

    private fun moveEntity(transform: TransformComponent, move: MoveComponent, deltaTime: Float) {
        //transform.position.x = MathUtils.clamp()
        transform.position.x += move.speed.x * deltaTime
        transform.position.y += move.speed.y * deltaTime
    }

    companion object {
        private val log = logger<MoveSystem>()
    }
}
