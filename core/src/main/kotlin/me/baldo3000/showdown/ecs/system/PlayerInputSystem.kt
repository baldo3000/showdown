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
import me.baldo3000.showdown.ecs.component.FacingComponent
import me.baldo3000.showdown.ecs.component.FacingDirection
import me.baldo3000.showdown.ecs.component.PlayerComponent
import me.baldo3000.showdown.ecs.component.TransformComponent

class PlayerInputSystem(
    private val gameViewport: Viewport
) : IteratingSystem(
    allOf(PlayerComponent::class, TransformComponent::class, FacingComponent::class).get()
) {
    private val tmpVector = Vector2()

    override fun processEntity(entity: Entity, deltaTime: Float) {
        val transform = entity[TransformComponent.mapper]
        require(transform != null) { "Entity must have a TransformComponent. Entity: $entity" }
        val facing = entity[FacingComponent.mapper]
        require(facing != null) { "Entity must have a FacingComponent. Entity: $entity" }

        /*tmpVector.x = Gdx.input.x.toFloat()
        tmpVector.y = Gdx.input.y.toFloat()
        gameViewport.unproject(tmpVector)
        val diffX = tmpVector.x - transform.position.x - transform.size.x * 0.5f
        val diffY = tmpVector.y - transform.position.y - transform.size.y * 0.5f
        val angleDeg =
            Math.toDegrees(kotlin.math.atan2(diffY.toDouble(), diffX.toDouble())).let { if (it < 0) it + 360.0 else it }
        facing.direction = when (angleDeg) {
            in 337.5..<360.0, in 0.0..<22.5 -> FacingDirection.RIGHT
            in 22.5..<67.5 -> FacingDirection.TOP_RIGHT
            in 67.5..<112.5 -> FacingDirection.TOP
            in 112.5..<157.5 -> FacingDirection.TOP_LEFT
            in 157.5..<202.5 -> FacingDirection.LEFT
            in 202.5..<247.5 -> FacingDirection.BOTTOM_LEFT
            in 247.5..<292.5 -> FacingDirection.BOTTOM
            in 292.5..<337.5 -> FacingDirection.BOTTOM_RIGHT
            else -> FacingDirection.DEFAULT
        }
        log.debug { "Player facing direction: ${facing.direction}, angleDeg: $angleDeg" }*/

        val top = Gdx.input.isKeyPressed(Input.Keys.W) || Gdx.input.isKeyPressed(Input.Keys.UP)
        val left = Gdx.input.isKeyPressed(Input.Keys.A) || Gdx.input.isKeyPressed(Input.Keys.LEFT)
        val bottom = Gdx.input.isKeyPressed(Input.Keys.S) || Gdx.input.isKeyPressed(Input.Keys.DOWN)
        val right = Gdx.input.isKeyPressed(Input.Keys.D) || Gdx.input.isKeyPressed(Input.Keys.RIGHT)

        val horizontal = (if (right) 1 else 0) - (if (left) 1 else 0)
        val vertical = (if (top) 1 else 0) - (if (bottom) 1 else 0)

        facing.direction = when {
            horizontal == 1 && vertical == 1 -> FacingDirection.TOP_RIGHT
            horizontal == 1 && vertical == 0 -> FacingDirection.RIGHT
            horizontal == 1 -> FacingDirection.BOTTOM_RIGHT
            horizontal == -1 && vertical == -1 -> FacingDirection.BOTTOM_LEFT
            horizontal == -1 && vertical == 0 -> FacingDirection.LEFT
            horizontal == -1 -> FacingDirection.TOP_LEFT
            vertical == 1 -> FacingDirection.TOP
            vertical == -1 -> FacingDirection.BOTTOM
            else -> FacingDirection.DEFAULT
        }
    }

    companion object {
        private val log = logger<PlayerInputSystem>()
    }
}
