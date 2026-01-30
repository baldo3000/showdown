package me.baldo3000.showdown.ecs.system

import com.badlogic.ashley.core.Entity
import com.badlogic.ashley.systems.IteratingSystem
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Input
import com.badlogic.gdx.math.MathUtils
import com.badlogic.gdx.math.Vector2
import ktx.ashley.allOf
import ktx.ashley.get
import ktx.log.logger
import me.baldo3000.showdown.ecs.component.*

class PlayerInputSystem() : IteratingSystem(
    allOf(PlayerComponent::class, TransformComponent::class, InputComponent::class, MoveComponent::class).get()
) {
    private val facing = Vector2()

    override fun processEntity(entity: Entity, deltaTime: Float) {
        val player = entity[PlayerComponent.mapper]
        require(player != null) { "Entity must have a PlayerComponent. Entity: $entity" }
        val transform = entity[TransformComponent.mapper]
        require(transform != null) { "Entity must have a TransformComponent. Entity: $entity" }
        val input = entity[InputComponent.mapper]
        require(input != null) { "Entity must have a InputComponent. Entity: $entity" }
        val move = entity[MoveComponent.mapper]
        require(move != null) { "Entity must have a MoveComponent. Entity: $entity" }

        val top = Gdx.input.isKeyPressed(Input.Keys.W) || Gdx.input.isKeyPressed(Input.Keys.UP)
        val left = Gdx.input.isKeyPressed(Input.Keys.A) || Gdx.input.isKeyPressed(Input.Keys.LEFT)
        val bottom = Gdx.input.isKeyPressed(Input.Keys.S) || Gdx.input.isKeyPressed(Input.Keys.DOWN)
        val right = Gdx.input.isKeyPressed(Input.Keys.D) || Gdx.input.isKeyPressed(Input.Keys.RIGHT)

        val horizontal = (if (right) 1 else 0) - (if (left) 1 else 0)
        val vertical = (if (top) 1 else 0) - (if (bottom) 1 else 0)

        facing.set(horizontal.toFloat(), vertical.toFloat()).nor()

        move.speed.x = if (facing.x != 0f) {
            move.speed.x + facing.x * ACCELERATION_FACTOR * deltaTime
        } else {
            0f
        }
        move.speed.y = if (facing.y != 0f) {
            move.speed.y + facing.y * ACCELERATION_FACTOR * deltaTime
        } else {
            0f
        }
        move.speed.x = MathUtils.clamp(move.speed.x, -MAX_SPEED, MAX_SPEED)
        move.speed.y = MathUtils.clamp(move.speed.y, -MAX_SPEED, MAX_SPEED)
    }

    companion object {
        private val log = logger<PlayerInputSystem>()
    }
}
