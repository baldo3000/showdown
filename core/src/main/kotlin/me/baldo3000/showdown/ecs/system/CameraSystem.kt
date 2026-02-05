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

class CameraSystem(private val gameViewPort: Viewport) : IteratingSystem(
    allOf(CameraComponent::class, TransformComponent::class).exclude(RemoveComponent::class).get()
) {
    override fun processEntity(entity: Entity, deltaTime: Float) {
        val transform = entity[TransformComponent.mapper]
        require(transform != null) { "Entity must have a TransformComponent. Entity: $entity" }
        val camera = entity[CameraComponent.mapper]
        require(camera != null) { "Entity must have a CameraComponent. Entity: $entity" }
        gameViewPort.camera.position.set(transform.position.x, transform.position.y, 1f)
        gameViewPort.camera.update()
    }

    companion object {
        private val log = logger<CameraSystem>()
    }
}
