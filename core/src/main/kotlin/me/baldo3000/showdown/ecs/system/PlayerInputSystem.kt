package me.baldo3000.showdown.ecs.system

import com.badlogic.ashley.core.Entity
import com.badlogic.ashley.systems.IteratingSystem
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Input
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.utils.viewport.Viewport
import ktx.ashley.allOf
import ktx.ashley.get
import ktx.log.logger
import me.baldo3000.showdown.ecs.component.InputComponent
import me.baldo3000.showdown.ecs.component.MoveComponent
import me.baldo3000.showdown.ecs.component.PlayerComponent
import me.baldo3000.showdown.ecs.component.TransformComponent
import me.baldo3000.showdown.ecs.createBullet
import me.baldo3000.showdown.network.Vector2D
import kotlin.uuid.Uuid

const val PLAYER_SPEED = 3f
private const val TOUCH_TOLERANCE_DISTANCE = 0.1f

class PlayerInputSystem(
    private val gameViewport: Viewport
) : IteratingSystem(
    allOf(PlayerComponent::class, TransformComponent::class, InputComponent::class, MoveComponent::class).get()
) {
    private val tmpVector = Vector2()

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

        tmpVector.set(horizontal.toFloat(), vertical.toFloat()).nor()

        move.speed.x = PLAYER_SPEED * tmpVector.x
        move.speed.y = PLAYER_SPEED * tmpVector.y

        if (Gdx.input.isTouched) {
            tmpVector.x = Gdx.input.x.toFloat()
            tmpVector.y = Gdx.input.y.toFloat()
            gameViewport.unproject(tmpVector)
            val distX = tmpVector.x - transform.position.x
            val distY = tmpVector.y - transform.position.y
            val speedVector = Vector2(distX, distY).nor()

            val bullet = engine.createBullet(
                Uuid.random(),
                Vector2D(transform.position.x, transform.position.y),
                Vector2D(speedVector.x * 5f, speedVector.y * 5f)
            )
            engine.addEntity(bullet)
        }
    }

    companion object {
        private val log = logger<PlayerInputSystem>()
    }
}
