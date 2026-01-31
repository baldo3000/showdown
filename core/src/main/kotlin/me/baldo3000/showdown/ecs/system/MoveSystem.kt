package me.baldo3000.showdown.ecs.system

import com.badlogic.ashley.core.Engine
import com.badlogic.ashley.core.Entity
import com.badlogic.ashley.systems.IteratingSystem
import ktx.ashley.allOf
import ktx.ashley.exclude
import ktx.ashley.get
import ktx.log.logger
import me.baldo3000.showdown.ecs.component.IdComponent
import me.baldo3000.showdown.ecs.component.MoveComponent
import me.baldo3000.showdown.ecs.component.RemoveComponent
import me.baldo3000.showdown.ecs.component.TransformComponent
import me.baldo3000.showdown.event.GameEventHandler
import me.baldo3000.showdown.event.api.GameEvent
import me.baldo3000.showdown.event.api.GameEventListener

private const val UPDATE_RATE = 1 / 60f

class MoveSystem(private val eventHandler: GameEventHandler) : IteratingSystem(
    allOf(TransformComponent::class, MoveComponent::class).exclude(RemoveComponent::class).get()
), GameEventListener {
    private var accumulator = 0f

    override fun addedToEngine(engine: Engine?) {
        super.addedToEngine(engine)
        eventHandler.addListener(GameEvent.PlayerSpeedChange::class, this)
    }

    override fun removedFromEngine(engine: Engine?) {
        super.removedFromEngine(engine)
        eventHandler.removeListener(GameEvent.PlayerSpeedChange::class, this)
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

        // log.debug { "Moving entity $entity with speed ${move.speed}" }
        moveEntity(transform, move, deltaTime)
    }

    private fun moveEntity(transform: TransformComponent, move: MoveComponent, deltaTime: Float) {
        //transform.position.x = MathUtils.clamp()
        transform.position.x += move.speed.x * deltaTime
        transform.position.y += move.speed.y * deltaTime
    }

    override fun onEvent(event: GameEvent) {
        if (event is GameEvent.PlayerSpeedChange) {
            entities.forEach { entity ->
                val id = entity[IdComponent.mapper] ?: return@forEach
                val move = entity[MoveComponent.mapper] ?: return@forEach
                if (id.id == event.id) {
                    move.speed.set(event.speed.x, event.speed.y)
                }
            }
        }
    }

    companion object {
        private val log = logger<MoveSystem>()
    }
}
