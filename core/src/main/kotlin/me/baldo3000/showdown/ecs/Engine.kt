package me.baldo3000.showdown.ecs

import com.badlogic.ashley.core.Engine
import com.badlogic.ashley.core.Entity
import com.badlogic.gdx.math.Circle
import com.badlogic.gdx.math.Rectangle
import ktx.ashley.*
import me.baldo3000.showdown.UNIT_SCALE
import me.baldo3000.showdown.data.Vector2D
import me.baldo3000.showdown.ecs.component.*
import me.baldo3000.showdown.ecs.component.event.PlayerJoinComponent
import me.baldo3000.showdown.ecs.component.event.SetupGameComponent
import me.baldo3000.showdown.ecs.system.PlayerInputSystem
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

private val characterFamily =
    allOf(
        InputComponent::class
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

private val wallsFamily =
    allOf(
        IdComponent::class,
        TransformComponent::class,
        ColliderComponent::class,
        GraphicComponent::class
    ).exclude(RemoveComponent::class).get()

val Engine.players: List<Entity>
    get() = getEntitiesFor(playersFamily).toList()

val Engine.character: Entity?
    get() = getEntitiesFor(characterFamily).firstOrNull()

val Engine.bullets: List<Entity>
    get() = getEntitiesFor(bulletsFamily).toList()

val Engine.walls: List<Entity>
    get() = getEntitiesFor(wallsFamily).toList()

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
            this.size.set(PLAYER_SIZE, PLAYER_SIZE)
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
            this.size.set(BULLET_SIZE, BULLET_SIZE)
        }
        with<ColliderComponent> { collider = Circle(position.x, position.y, BULLET_SIZE / 2) }
        with<MoveComponent> {
            this.speed.set(speed)
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

fun Engine.createWall(wallId: Uuid = Uuid.random(), position: Vector2D, size: Vector2D): Entity {
    return entity {
        with<TransformComponent> {
            this.setInitialPosition(position.x, position.y, 0f)
            this.size.set(size)
        }
        with<GraphicComponent> {
            sprite.run {
                setRegion(Textures.wallTexture)
                setSize(texture.width * UNIT_SCALE, texture.height * UNIT_SCALE)
                setOriginCenter()
            }
        }
        with<ColliderComponent> {
            collider = Rectangle(position.x - size.x / 2f, position.y - size.y / 2f, size.x, size.y)
        }
        with<IdComponent> { id = wallId }
    }
}

fun Engine.spawnWallsFromWorld(world: ShowdownWorld) {
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

// Events spawning

fun Engine.spawnPlayer(playerId: Uuid, controllable: Boolean) {
    entity {
        with<PlayerJoinComponent> {
            this.playerId = playerId
            this.controllable = controllable
        }
    }
}

fun Engine.spawnWalls() {
    entity { with<SetupGameComponent>() }
}

var Engine.processingInput: Boolean
    get() = try {
        getSystem<PlayerInputSystem>().inputEnabled
    } catch (_: MissingEntitySystemException) {
        false
    }
    set(value) {
        try {
            getSystem<PlayerInputSystem>().inputEnabled = value
        } catch (_: MissingEntitySystemException) {
        }
    }
