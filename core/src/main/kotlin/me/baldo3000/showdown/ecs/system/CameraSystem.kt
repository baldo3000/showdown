package me.baldo3000.showdown.ecs.system

import com.badlogic.ashley.core.Entity
import com.badlogic.ashley.systems.IteratingSystem
import com.badlogic.gdx.utils.viewport.Viewport
import ktx.ashley.allOf
import ktx.ashley.exclude
import ktx.ashley.get
import ktx.log.logger
import me.baldo3000.showdown.ecs.component.CameraComponent
import me.baldo3000.showdown.ecs.component.RemoveComponent
import me.baldo3000.showdown.ecs.component.TransformComponent

class CameraSystem(private val gameViewport: Viewport) : IteratingSystem(
    allOf(CameraComponent::class, TransformComponent::class).exclude(RemoveComponent::class).get()
) {
    override fun processEntity(entity: Entity, deltaTime: Float) {
        val transform = entity[TransformComponent.mapper]
        require(transform != null) { "Entity must have a TransformComponent. Entity: $entity" }
        val camera = entity[CameraComponent.mapper]
        require(camera != null) { "Entity must have a CameraComponent. Entity: $entity" }
        gameViewport.camera.position.set(transform.interpolatedPosition.x, transform.interpolatedPosition.y, 1f)
        gameViewport.camera.update()
    }

    companion object {
        private val log = logger<CameraSystem>()
    }
}
