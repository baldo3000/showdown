package me.baldo3000.showdown.game

import com.badlogic.ashley.core.Engine
import com.badlogic.ashley.core.Entity
import com.badlogic.gdx.math.Circle
import com.badlogic.gdx.math.Rectangle
import ktx.ashley.entity
import ktx.ashley.with
import me.baldo3000.showdown.UNIT_SCALE
import me.baldo3000.showdown.data.Vector2D
import me.baldo3000.showdown.ecs.component.*
import me.baldo3000.showdown.ui.Textures
import kotlin.uuid.Uuid

private const val PLAYER_SIZE = 1f
private const val BULLET_SIZE = 0.25f
private const val WALL_THICKNESS = 0.5f
private const val DEFAULT_DAMAGE = 5f

class EntityFactory(private val engine: Engine) {

    fun createPlayer(
        playerId: Uuid,
        position: Vector2D,
        controllable: Boolean = false
    ): Entity {
        return engine.entity {
            if (controllable) {
                with<InputComponent>()
                with<CameraComponent>()
            }
            with<IdComponent> { id = playerId }
            with<TransformComponent> {
                setInitialPosition(position.x, position.y, 0f)
                size.set(PLAYER_SIZE, PLAYER_SIZE)
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

    fun createBullet(
        bulletId: Uuid,
        sourceId: Uuid?,
        damage: Float = DEFAULT_DAMAGE,
        position: Vector2D,
        speed: Vector2D
    ): Entity {
        return engine.entity {
            with<IdComponent> { id = bulletId }
            with<TransformComponent> {
                setInitialPosition(position.x, position.y, -1f)
                size.set(BULLET_SIZE, BULLET_SIZE)
            }
            with<ColliderComponent> { collider = Circle(position.x, position.y, BULLET_SIZE / 2) }
            with<MoveComponent> { this.speed.set(speed) }
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

    fun createWall(wallId: Uuid = Uuid.random(), position: Vector2D, size: Vector2D): Entity {
        return engine.entity {
            with<TransformComponent> {
                setInitialPosition(position.x, position.y, 0f)
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

    fun createWallsFromMapSize(mapSize: Vector2D) {
        val width = mapSize.x
        val height = mapSize.y
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
}
