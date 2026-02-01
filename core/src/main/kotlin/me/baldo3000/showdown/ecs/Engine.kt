package me.baldo3000.showdown.ecs

import com.badlogic.ashley.core.Engine
import com.badlogic.ashley.core.Entity
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.Pixmap
import com.badlogic.gdx.graphics.Texture
import ktx.ashley.get
import me.baldo3000.showdown.UNIT_SCALE
import me.baldo3000.showdown.ecs.component.*
import kotlin.uuid.Uuid

val Engine.players: List<Entity>
    get() = entities.filter { entity ->
        entity[IdComponent.mapper] != null &&
            entity[PlayerComponent.mapper] != null &&
            entity[TransformComponent.mapper] != null &&
            entity[MoveComponent.mapper] != null &&
            entity[RemoveComponent.mapper] == null
    }

fun Engine.createPlayer(
    playerId: Uuid,
    spawnX: Float = 1f,
    spawnY: Float = 1f,
    controllable: Boolean = false
): Entity {
    val size = 32
    val redPixmap = Pixmap(size, size, Pixmap.Format.RGBA8888).apply {
        setColor(0f, 0f, 0f, 0f)
        fill()
        // draw filled circle centered in pixmap
        setColor(Color.RED)
        fillCircle(size / 2, size / 2, size / 2 - 1)

    }
    val redTexture = Texture(redPixmap)
    redPixmap.dispose()
    return createEntity().apply {
        if (controllable) add(InputComponent())
        add(IdComponent().apply { id = playerId })
        add(TransformComponent())
        add(MoveComponent())
        add(PlayerComponent())
        add(GraphicComponent().apply {
            sprite.run {
                setRegion(redTexture)
                setSize(texture.width * UNIT_SCALE, texture.height * UNIT_SCALE)
                setOriginCenter()
            }
        })
    }
}
