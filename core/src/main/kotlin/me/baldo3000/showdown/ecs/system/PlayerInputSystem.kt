package me.baldo3000.showdown.ecs.system

import com.badlogic.ashley.core.Engine
import com.badlogic.ashley.core.Entity
import com.badlogic.ashley.core.EntitySystem
import com.badlogic.gdx.Input
import com.badlogic.gdx.utils.viewport.Viewport
import ktx.ashley.allOf
import ktx.ashley.get
import ktx.log.logger
import me.baldo3000.showdown.data.Vector2D
import me.baldo3000.showdown.ecs.component.*
import me.baldo3000.showdown.game.EntityFactory
import me.baldo3000.showdown.game.GameState
import me.baldo3000.showdown.input.DummyInputProcessor
import me.baldo3000.showdown.input.addInputProcessor
import me.baldo3000.showdown.input.removeInputProcessor
import kotlin.uuid.Uuid

const val PLAYER_SPEED = 3f

class PlayerInputSystem(
    private val gameViewport: Viewport,
    private val entityFactory: EntityFactory,
    private val gameState: GameState
) : EntitySystem(), DummyInputProcessor {
    private val family = allOf(InputComponent::class, TransformComponent::class, MoveComponent::class).get()
    private val entities
        get() = engine.getEntitiesFor(family)

    private val tmpSpeedVector = Vector2D()
    private val tmpShootVector = Vector2D()
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
        removeInputProcessor(this)
        super.removedFromEngine(engine)
    }

    private fun updateEntitySpeeds() {
        if (gameState.inputEnabled) entities.forEach(::updateEntitySpeed)
    }

    private fun shootFromEntities() {
        if (gameState.inputEnabled) entities.forEach(::shootFromEntity)
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

        tmpSpeedVector.set(
            tmpShootVector.x - transform.position.x,
            tmpShootVector.y - transform.position.y
        ).nor()

        entityFactory.createBullet(
            Uuid.random(),
            entity[IdComponent.mapper]?.id,
            DEFAULT_DAMAGE,
            transform.position.to2D(),
            Vector2D(tmpSpeedVector.x * 5f, tmpSpeedVector.y * 5f)
        )
    }

    override fun keyDown(keycode: Int): Boolean {
        when (keycode) {
            Input.Keys.W, Input.Keys.UP -> vertical++
            Input.Keys.S, Input.Keys.DOWN -> vertical--
            Input.Keys.A, Input.Keys.LEFT -> horizontal--
            Input.Keys.D, Input.Keys.RIGHT -> horizontal++
        }
        updateEntitySpeeds()
        return super.keyDown(keycode)
    }

    override fun keyUp(keycode: Int): Boolean {
        when (keycode) {
            Input.Keys.W, Input.Keys.UP -> vertical--
            Input.Keys.S, Input.Keys.DOWN -> vertical++
            Input.Keys.A, Input.Keys.LEFT -> horizontal++
            Input.Keys.D, Input.Keys.RIGHT -> horizontal--
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
