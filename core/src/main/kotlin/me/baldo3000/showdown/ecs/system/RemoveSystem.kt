package me.baldo3000.showdown.ecs.system

import com.badlogic.ashley.core.Entity
import com.badlogic.ashley.systems.IteratingSystem
import ktx.ashley.allOf
import ktx.ashley.get
import ktx.log.logger
import me.baldo3000.showdown.ecs.component.RemoveComponent

class RemoveSystem : IteratingSystem(allOf(RemoveComponent::class).get()) {
    override fun processEntity(entity: Entity, deltaTime: Float) {
        val remove = entity[RemoveComponent.mapper]
        require(remove != null) { "Entity must have a RemoveComponent. Entity: $entity" }

        remove.delay -= deltaTime
        if (remove.delay <= 0f) {
            engine.removeEntity(entity)
        }
    }

    companion object {
        private val log = logger<RemoveSystem>()
    }
}
