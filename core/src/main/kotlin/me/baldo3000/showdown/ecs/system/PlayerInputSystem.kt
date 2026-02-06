package me.baldo3000.showdown.ecs.system

import com.badlogic.ashley.core.Engine
import com.badlogic.ashley.core.Entity
import com.badlogic.ashley.core.EntitySystem
import com.badlogic.gdx.Input
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.utils.viewport.Viewport
import ktx.ashley.allOf
import ktx.ashley.get
import ktx.log.logger
import me.baldo3000.showdown.ecs.component.*
import me.baldo3000.showdown.ecs.createBullet
import me.baldo3000.showdown.input.DummyInputProcessor
import me.baldo3000.showdown.input.addInputProcessor
import me.baldo3000.showdown.network.Vector2D
import kotlin.uuid.Uuid

const val PLAYER_SPEED = 3f

class PlayerInputSystem(
    private val gameViewport: Viewport
) : EntitySystem(), DummyInputProcessor {

    private val family = allOf(InputComponent::class, TransformComponent::class, MoveComponent::class).get()
    private val entities
        get() = engine.getEntitiesFor(family)

    private val tmpSpeedVector = Vector2()
    private val tmpShootVector = Vector2()
    private var horizontal = 0
    private var vertical = 0

    init {
        setProcessing(false)
    }

    override fun addedToEngine(engine: Engine) {
        addInputProcessor(this)
        super.addedToEngine(engine)
    }

    override fun removedFromEngine(engine: Engine) {
        addInputProcessor(this)
        super.removedFromEngine(engine)
    }

    private fun updateEntitySpeeds() {
        entities.forEach(::updateEntitySpeed)
    }

    private fun shootFromEntities() {
        entities.forEach(::shootFromEntity)
    }

    private fun updateEntitySpeed(entity: Entity) {
        val transform = entity[TransformComponent.mapper]
        require(transform != null) { "Entity must have a TransformComponent. Entity: $entity" }
        val input = entity[InputComponent.mapper]
        require(input != null) { "Entity must have a InputComponent. Entity: $entity" }
        val move = entity[MoveComponent.mapper]
        require(move != null) { "Entity must have a MoveComponent. Entity: $entity" }

        tmpSpeedVector.set(horizontal.toFloat(), vertical.toFloat()).nor()

        move.speed.x = PLAYER_SPEED * tmpSpeedVector.x
        move.speed.y = PLAYER_SPEED * tmpSpeedVector.y
    }

    private fun shootFromEntity(entity: Entity) {
        val transform = entity[TransformComponent.mapper]
        require(transform != null) { "Entity must have a TransformComponent. Entity: $entity" }
        val input = entity[InputComponent.mapper]
        require(input != null) { "Entity must have a InputComponent. Entity: $entity" }
        val move = entity[MoveComponent.mapper]
        require(move != null) { "Entity must have a MoveComponent. Entity: $entity" }

        val distX = tmpShootVector.x - transform.position.x
        val distY = tmpShootVector.y - transform.position.y
        val speedVector = Vector2(distX, distY).nor()

        engine.createBullet(
            Uuid.random(),
            entity[IdComponent.mapper]?.id,
            DEFAULT_DAMAGE,
            Vector2D(transform.position.x, transform.position.y),
            Vector2D(speedVector.x * 5f, speedVector.y * 5f)
        )
    }

    override fun keyDown(keycode: Int): Boolean {
        when (keycode) {
            Input.Keys.W, Input.Keys.UP -> vertical += 1
            Input.Keys.S, Input.Keys.DOWN -> vertical -= 1
            Input.Keys.A, Input.Keys.LEFT -> horizontal -= 1
            Input.Keys.D, Input.Keys.RIGHT -> horizontal += 1
        }
        updateEntitySpeeds()
        return super.keyDown(keycode)
    }

    override fun keyUp(keycode: Int): Boolean {
        when (keycode) {
            Input.Keys.W, Input.Keys.UP -> vertical -= 1
            Input.Keys.S, Input.Keys.DOWN -> vertical += 1
            Input.Keys.A, Input.Keys.LEFT -> horizontal += 1
            Input.Keys.D, Input.Keys.RIGHT -> horizontal -= 1
        }
        updateEntitySpeeds()
        return super.keyUp(keycode)
    }

    override fun touchDown(screenX: Int, screenY: Int, pointer: Int, button: Int): Boolean {
        tmpShootVector.set(screenX.toFloat(), screenY.toFloat())
        gameViewport.unproject(tmpShootVector)
        shootFromEntities()
        return super.touchDown(screenX, screenY, pointer, button)
    }

    companion object {
        private val log = logger<PlayerInputSystem>()
    }
}
