package me.baldo3000.showdown.ecs

import com.badlogic.ashley.core.Engine
import com.badlogic.ashley.core.Entity
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.Pixmap
import com.badlogic.gdx.graphics.Texture
import ktx.ashley.allOf
import ktx.ashley.entity
import ktx.ashley.exclude
import ktx.ashley.with
import me.baldo3000.showdown.UNIT_SCALE
import me.baldo3000.showdown.ecs.component.*
import me.baldo3000.showdown.network.Vector2D
import me.baldo3000.showdown.world.ShowdownWorld
import kotlin.math.max
import kotlin.math.roundToInt
import kotlin.uuid.Uuid

private val playersFamily =
    allOf(
        IdComponent::class,
        HealthComponent::class,
        TransformComponent::class,
        MoveComponent::class,
        ColliderComponent::class,
        GraphicComponent::class
    ).exclude(RemoveComponent::class).get()

private val bulletsFamily =
    allOf(
        IdComponent::class,
        DamageComponent::class,
        TransformComponent::class,
        MoveComponent::class,
        ColliderComponent::class,
        GraphicComponent::class
    ).exclude(RemoveComponent::class).get()

val Engine.players: List<Entity>
    get() = getEntitiesFor(playersFamily).toList()

val Engine.bullets: List<Entity>
    get() = getEntitiesFor(bulletsFamily).toList()

fun Engine.createPlayer(
    playerId: Uuid,
    position: Vector2D = Vector2D(8f, 4.5f),
    controllable: Boolean = false
): Entity {
    val size = 64
    val pixmap = Pixmap(size, size, Pixmap.Format.RGBA8888).apply {
        setColor(Color.CYAN)
        fillCircle(size / 2, size / 2, size / 2 - 1)
    }
    val redTexture = Texture(pixmap)
    pixmap.dispose()

    return entity {
        if (controllable) {
            with<InputComponent>()
            with<CameraComponent>()
        }
        with<IdComponent> { id = playerId }
        with<TransformComponent> {
            this.position.x = position.x
            this.position.y = position.y
            this.size.x = 1f
            this.size.y = 1f
        }
        with<ColliderComponent> { collider.set(position.x, position.y, 1f / 2) }
        with<MoveComponent>()
        with<HealthComponent>()
        with<GraphicComponent> {
            sprite.run {
                setRegion(redTexture)
                setSize(texture.width * UNIT_SCALE, texture.height * UNIT_SCALE)
                setOriginCenter()
            }
        }
    }
}

fun Engine.createBullet(
    bulletId: Uuid,
    sourceId: Uuid?,
    damage: Float = DEFAULT_DAMAGE,
    position: Vector2D,
    speed: Vector2D
): Entity {
    val size = 4
    val pixmap = Pixmap(size, size, Pixmap.Format.RGBA8888).apply {
        setColor(Color.RED)
        fillCircle(size / 2, size / 2, size / 2 - 1)
    }
    val redTexture = Texture(pixmap)
    pixmap.dispose()

    return entity {
        with<IdComponent> { id = bulletId }
        with<TransformComponent> {
            this.position.x = position.x
            this.position.y = position.y
            this.position.z = -1f
            this.size.x = 0.25f
            this.size.y = 0.25f
        }
        with<ColliderComponent> { collider.set(position.x, position.y, 0.25f / 2) }
        with<MoveComponent> {
            this.speed.x = speed.x
            this.speed.y = speed.y
        }
        with<GraphicComponent> {
            sprite.run {
                setRegion(redTexture)
                setSize(texture.width * UNIT_SCALE, texture.height * UNIT_SCALE)
                setOriginCenter()
            }
        }
        with<DamageComponent> {
            this.sourceId = sourceId
            this.damage = damage
        }
    }
}

fun Engine.createWall(position: Vector2D, size: Vector2D): Entity {
    val pxW = max(1, (size.x / UNIT_SCALE).roundToInt())
    val pxH = max(1, (size.y / UNIT_SCALE).roundToInt())
    val pixmap = Pixmap(pxW, pxH, Pixmap.Format.RGBA8888).apply {
        setColor(Color.RED)
        fillRectangle(0, 0, pxW, pxH)
    }
    val texture = Texture(pixmap)
    pixmap.dispose()

    return entity {
        with<TransformComponent> {
            this.position.x = position.x
            this.position.y = position.y
            this.size.x = size.x
            this.size.y = size.y
        }
        with<GraphicComponent> {
            sprite.run {
                setRegion(texture)
                setSize(texture.width * UNIT_SCALE, texture.height * UNIT_SCALE)
                setOriginCenter()
            }
        }
        /*add(ColliderComponent().apply {
            val radius = max(size.x, size.y) / 2f
            collider.set(position.x, position.y, radius)
        })*/
    }
}

fun Engine.initializeWorld(world: ShowdownWorld) {
    val thickness = 0.5f

    val w = world.mapSize.x
    val h = world.mapSize.y

    // Left wall
    createWall(
        position = Vector2D(-thickness / 2f, h / 2f),
        size = Vector2D(thickness, h)
    )

    // Right wall
    createWall(
        position = Vector2D(w + thickness / 2f, h / 2f),
        size = Vector2D(thickness, h)
    )

    // Bottom wall
    createWall(
        position = Vector2D(w / 2f, -thickness / 2f),
        size = Vector2D(w + 2f * thickness, thickness)
    )

    // Top wall
    createWall(
        position = Vector2D(w / 2f, h + thickness / 2f),
        size = Vector2D(w + 2f * thickness, thickness)
    )
}

fun Engine.reset() {
    removeAllEntities()
}
