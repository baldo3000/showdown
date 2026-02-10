package me.baldo3000.showdown.ecs.system

import com.badlogic.ashley.core.Entity
import com.badlogic.ashley.systems.IteratingSystem
import ktx.ashley.exclude
import ktx.ashley.get
import ktx.ashley.oneOf
import ktx.log.logger
import me.baldo3000.showdown.ecs.component.RemoveComponent
import me.baldo3000.showdown.ecs.component.event.DefeatComponent
import me.baldo3000.showdown.ecs.component.event.EventComponent
import me.baldo3000.showdown.ecs.component.event.VictoryComponent

class GameEventsSystem : IteratingSystem(
    oneOf(DefeatComponent::class, VictoryComponent::class).exclude(RemoveComponent::class).get()
) {
    override fun processEntity(entity: Entity, deltaTime: Float) {
        val event: EventComponent = entity[DefeatComponent.mapper]
            ?: entity[VictoryComponent.mapper]
            ?: throw IllegalArgumentException("Entity must be an event")

        processEvent(event)
        entity.add(RemoveComponent())
    }

    private fun processEvent(event: EventComponent) {
        when (event) {
            is DefeatComponent -> log.info { "Game lost!" }
            is VictoryComponent -> log.info { "Game won!" }
        }
    }

    companion object {
        private val log = logger<GameEventsSystem>()
    }
}
