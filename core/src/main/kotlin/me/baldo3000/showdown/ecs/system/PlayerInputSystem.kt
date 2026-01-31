package me.baldo3000.showdown.ecs.system

import com.badlogic.ashley.core.Entity
import com.badlogic.ashley.systems.IteratingSystem
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Input
import com.badlogic.gdx.math.Vector2
import ktx.ashley.allOf
import ktx.ashley.get
import ktx.log.logger
import me.baldo3000.showdown.ecs.component.InputComponent
import me.baldo3000.showdown.ecs.component.MoveComponent
import me.baldo3000.showdown.ecs.component.PlayerComponent
import me.baldo3000.showdown.ecs.component.TransformComponent

const val PLAYER_SPEED = 3f

class PlayerInputSystem() : IteratingSystem(
    allOf(PlayerComponent::class, TransformComponent::class, InputComponent::class, MoveComponent::class).get()
) {
    private val speedVector = Vector2()

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

        speedVector.set(horizontal.toFloat(), vertical.toFloat()).nor()

        move.speed.x = PLAYER_SPEED * speedVector.x
        move.speed.y = PLAYER_SPEED * speedVector.y
    }

    companion object {
        private val log = logger<PlayerInputSystem>()
    }
}
