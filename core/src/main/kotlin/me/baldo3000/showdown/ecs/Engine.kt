package me.baldo3000.showdown.ecs

import com.badlogic.ashley.core.Engine
import com.badlogic.ashley.core.Entity
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.Pixmap
import com.badlogic.gdx.graphics.Texture
import ktx.ashley.get
import me.baldo3000.showdown.UNIT_SCALE
import me.baldo3000.showdown.ecs.component.*
import me.baldo3000.showdown.network.Vector2D
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
    position: Vector2D = Vector2D(8f, 4.5f),
    controllable: Boolean = false
): Entity {
    val size = 64
    val pixmap = Pixmap(size, size, Pixmap.Format.RGBA8888).apply {
        //setColor(0f, 0f, 0f, 0f)
        //fill()
        // draw filled circle centered in pixmap
        setColor(Color.CYAN)
        fillCircle(size / 2, size / 2, size / 2 - 1)

    }
    val redTexture = Texture(pixmap)
    pixmap.dispose()
    return createEntity().apply {
        if (controllable) add(InputComponent())
        add(IdComponent().apply { id = playerId })
        add(TransformComponent().apply {
            this.position.x = position.x
            this.position.y = position.y
        })
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

fun Engine.createBullet(
    bulletId: Uuid,
    position: Vector2D,
    speed: Vector2D
): Entity {
    val size = 4
    val pixmap = Pixmap(size, size, Pixmap.Format.RGBA8888).apply {
        //setColor(0f, 0f, 0f, 0f)
        //fill()
        // draw filled circle centered in pixmap
        setColor(Color.RED)
        fillCircle(size / 2, size / 2, size / 2 - 1)
    }
    val redTexture = Texture(pixmap)
    pixmap.dispose()
    return createEntity().apply {
        add(IdComponent().apply { id = bulletId })
        add(TransformComponent().apply {
            this.position.x = position.x
            this.position.y = position.y
            this.size.x = 0.25f
            this.size.y = 0.25f
        })
        add(MoveComponent().apply {
            this.speed.x = speed.x
            this.speed.y = speed.y
        })
        add(GraphicComponent().apply {
            sprite.run {
                setRegion(redTexture)
                setSize(texture.width * UNIT_SCALE, texture.height * UNIT_SCALE)
                setOriginCenter()
            }
        })
    }
}
