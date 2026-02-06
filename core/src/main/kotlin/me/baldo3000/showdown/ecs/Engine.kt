package me.baldo3000.showdown.ecs

import com.badlogic.ashley.core.Engine
import com.badlogic.ashley.core.Entity
import com.badlogic.gdx.math.Circle
import ktx.ashley.allOf
import ktx.ashley.entity
import ktx.ashley.exclude
import ktx.ashley.with
import me.baldo3000.showdown.UNIT_SCALE
import me.baldo3000.showdown.ecs.component.*
import me.baldo3000.showdown.network.Vector2D
import me.baldo3000.showdown.ui.Textures
import me.baldo3000.showdown.world.ShowdownWorld
import kotlin.uuid.Uuid

const val PLAYER_SIZE = 1f
const val BULLET_SIZE = 0.25f
const val WALL_THICKNESS = 0.5f

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
    return entity {
        if (controllable) {
            with<InputComponent>()
            with<CameraComponent>()
        }
        with<IdComponent> { id = playerId }
        with<TransformComponent> {
            this.setInitialPosition(position.x, position.y, 0f)
            this.size.x = PLAYER_SIZE
            this.size.y = PLAYER_SIZE
        }
        with<ColliderComponent> { collider = Circle(position.x, position.y, PLAYER_SIZE / 2) }
        with<MoveComponent>()
        with<HealthComponent>()
        with<GraphicComponent> {
            sprite.run {
                setRegion(Textures.playerTexture)
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
    return entity {
        with<IdComponent> { id = bulletId }
        with<TransformComponent> {
            this.setInitialPosition(position.x, position.y, -1f)
            this.size.x = BULLET_SIZE
            this.size.y = BULLET_SIZE
        }
        with<ColliderComponent> { collider = Circle(position.x, position.y, BULLET_SIZE / 2) }
        with<MoveComponent> {
            this.speed.x = speed.x
            this.speed.y = speed.y
        }
        with<GraphicComponent> {
            sprite.run {
                setRegion(Textures.bulletTexture)
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
    return entity {
        with<TransformComponent> {
            this.setInitialPosition(position.x, position.y, 0f)
            this.size.x = size.x
            this.size.y = size.y
        }
        with<GraphicComponent> {
            sprite.run {
                setRegion(Textures.wallTexture)
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
    val width = world.mapSize.x
    val height = world.mapSize.y

    // Left wall
    createWall(
        position = Vector2D(-WALL_THICKNESS / 2f, height / 2f),
        size = Vector2D(WALL_THICKNESS, height)
    )

    // Right wall
    createWall(
        position = Vector2D(width + WALL_THICKNESS / 2f, height / 2f),
        size = Vector2D(WALL_THICKNESS, height)
    )

    // Bottom wall
    createWall(
        position = Vector2D(width / 2f, -WALL_THICKNESS / 2f),
        size = Vector2D(width + 2f * WALL_THICKNESS, WALL_THICKNESS)
    )

    // Top wall
    createWall(
        position = Vector2D(width / 2f, height + WALL_THICKNESS / 2f),
        size = Vector2D(width + 2f * WALL_THICKNESS, WALL_THICKNESS)
    )
}

fun Engine.reset() {
    removeAllEntities()
}
