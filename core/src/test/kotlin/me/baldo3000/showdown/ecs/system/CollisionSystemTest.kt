package me.baldo3000.showdown.ecs.system

import com.badlogic.ashley.core.PooledEngine
import com.badlogic.gdx.math.Circle
import ktx.ashley.entity
import ktx.ashley.get
import ktx.ashley.with
import me.baldo3000.showdown.ecs.component.*
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@OptIn(ExperimentalUuidApi::class)
class CollisionSystemTest {

    private lateinit var engine: PooledEngine

    @BeforeEach
    fun setUp() {
        engine = PooledEngine()
        engine.addSystem(CollisionSystem())
    }

    private fun addPlayer(x: Float, y: Float, radius: Float = 0.5f) = engine.entity {
        with<TransformComponent> { setInitialPosition(x, y, 0f) }
        with<MoveComponent> { speed.set(10f, 0f) }
        with<ColliderComponent> { collider = Circle(x, y, radius) }
        with<HealthComponent>()
        with<IdComponent>()
    }

    private fun addBullet(
        x: Float,
        y: Float,
        sourceId: Uuid? = null,
        damage: Float = DEFAULT_DAMAGE,
        radius: Float = 0.25f
    ) = engine.entity {
        with<TransformComponent> { setInitialPosition(x, y, 0f) }
        with<MoveComponent>()
        with<ColliderComponent> { collider = Circle(x, y, radius) }
        with<DamageComponent> {
            this.damage = damage
            this.sourceId = sourceId
        }
    }

    private fun addWall(x: Float, y: Float, radius: Float = 0.5f) = engine.entity {
        with<TransformComponent> { setInitialPosition(x, y, 0f) }
        with<MoveComponent>()
        with<ColliderComponent> { collider = Circle(x, y, radius) }
    }


    @Test
    fun `non-overlapping entities are not affected`() {
        val player = addPlayer(0f, 0f)
        addPlayer(100f, 100f)

        val transform = player[TransformComponent.mapper]!!
        val xBefore = transform.position.x

        engine.update(1f / 60f)

        assertEquals(xBefore, transform.position.x, 1e-4f)
        assertNull(player[RemoveComponent.mapper])
    }

    @Test
    fun `player colliding with wall is pushed back`() {
        val player = addPlayer(0f, 0f, radius = 0.5f)
        addWall(0.4f, 0f, radius = 0.5f)

        val transform = player[TransformComponent.mapper]!!
        val xPrev = transform.position.x   // 0

        engine.update(1f / 60f)

        assertTrue(transform.position.x < xPrev, "Player should have been pushed back on wall collision")
    }

    @Test
    fun `bullet hitting player reduces player health`() {
        val player = addPlayer(0f, 0f)
        addBullet(0.1f, 0f, sourceId = Uuid.random(), damage = 25f)

        val health = player[HealthComponent.mapper]!!
        engine.update(1f / 60f)

        assertTrue(health.health < MAX_HEALTH, "Player health should have been reduced")
    }

    @Test
    fun `bullet hitting player gets RemoveComponent`() {
        val bullet = addBullet(0.1f, 0f, sourceId = Uuid.random())
        addPlayer(0f, 0f)

        engine.update(1f / 60f)

        assertNotNull(bullet[RemoveComponent.mapper], "Bullet should be marked for removal after hitting player")
    }

    @Test
    fun `bullet does not hit its own source player`() {
        val playerId = Uuid.random()
        val player = engine.entity {
            with<TransformComponent> { setInitialPosition(0f, 0f, 0f) }
            with<MoveComponent>()
            with<ColliderComponent> { collider = Circle(0f, 0f, 0.5f) }
            with<HealthComponent>()
            with<IdComponent> { id = playerId }
        }
        val bullet = addBullet(0.1f, 0f, sourceId = playerId)

        val health = player[HealthComponent.mapper]!!
        engine.update(1f / 60f)

        assertEquals(MAX_HEALTH, health.health, "Bullet should not damage its own source player")
        assertNull(bullet[RemoveComponent.mapper], "Bullet should not be removed when hitting its own source")
    }

    @Test
    fun `player killed by bullet gets RemoveComponent`() {
        val player = addPlayer(0f, 0f)
        addBullet(0.1f, 0f, sourceId = Uuid.random(), damage = MAX_HEALTH)

        engine.update(1f / 60f)

        assertNotNull(player[RemoveComponent.mapper], "Dead player should be marked for removal")
    }

    @Test
    fun `entity with RemoveComponent is excluded from collision processing`() {
        val player = addPlayer(0f, 0f)
        val bullet = addBullet(0.1f, 0f, sourceId = Uuid.random(), damage = MAX_HEALTH)
        bullet.add(RemoveComponent())

        val health = player[HealthComponent.mapper]!!
        engine.update(1f / 60f)

        assertEquals(MAX_HEALTH, health.health, "Removed bullet should not inflict damage")
    }
}
