package me.baldo3000.showdown.ecs.system

import com.badlogic.ashley.core.Entity
import com.badlogic.ashley.systems.SortedIteratingSystem
import com.badlogic.gdx.graphics.g2d.Batch
import com.badlogic.gdx.utils.viewport.Viewport
import ktx.ashley.allOf
import ktx.ashley.get
import ktx.graphics.use
import ktx.log.logger
import me.baldo3000.showdown.ecs.component.GraphicComponent
import me.baldo3000.showdown.ecs.component.TransformComponent

class RenderSystem(
    private val batch: Batch,
    private val gameViewport: Viewport
) : SortedIteratingSystem(
    allOf(TransformComponent::class, GraphicComponent::class).get(),
    compareBy { entity -> entity[TransformComponent.mapper] }
) {
    override fun update(deltaTime: Float) {
        forceSort()
        gameViewport.apply()
        batch.use(gameViewport.camera.combined) {
            super.update(deltaTime)
        }
    }

    override fun processEntity(entity: Entity, deltaTime: Float) {
        val transform = entity[TransformComponent.mapper]
        require(transform != null) { "Entity must have a TransformComponent. Entity: $entity" }
        val graphic = entity[GraphicComponent.mapper]
        require(graphic != null) { "Entity must have a GraphicComponent. Entity: $entity" }

        if (graphic.sprite.texture == null) {
            log.error { "Entity has no texture for rendering. Entity: $entity" }
            return
        }

        graphic.sprite.run {
            rotation = transform.rotationDeg
            setBounds(transform.position.x, transform.position.y, transform.size.x, transform.size.y)
            draw(batch)
        }
    }

    companion object {
        private val log = logger<RenderSystem>()
    }
}
