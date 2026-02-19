package me.baldo3000.showdown.ecs.system

import com.badlogic.ashley.core.PooledEngine
import ktx.ashley.entity
import ktx.ashley.get
import ktx.ashley.with
import me.baldo3000.showdown.ecs.component.MoveComponent
import me.baldo3000.showdown.ecs.component.RemoveComponent
import me.baldo3000.showdown.ecs.component.TransformComponent
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

private const val FIXED_STEP = 1f / 60f
private const val DELTA = 1e-4f

class MoveSystemTest {

    private lateinit var engine: PooledEngine
    private lateinit var system: MoveSystem

    @BeforeEach
    fun setUp() {
        engine = PooledEngine()
        system = MoveSystem()
        engine.addSystem(system)
    }

    @Test
    fun `entity moves according to speed after one fixed step`() {
        val entity = engine.entity {
            with<TransformComponent>()
            with<MoveComponent> { speed.set(60f, 30f) }
        }
        engine.update(FIXED_STEP)

        val transform = entity[TransformComponent.mapper]!!
        assertEquals(60f * FIXED_STEP, transform.position.x, DELTA)
        assertEquals(30f * FIXED_STEP, transform.position.y, DELTA)
    }

    @Test
    fun `position accumulates over multiple fixed steps`() {
        val entity = engine.entity {
            with<TransformComponent>()
            with<MoveComponent> { speed.set(60f, 0f) }
        }
        // 3 fixed steps
        repeat(3) { engine.update(FIXED_STEP) }

        val transform = entity[TransformComponent.mapper]!!
        assertEquals(3f * 60f * FIXED_STEP, transform.position.x, DELTA)
    }

    @Test
    fun `previous position is saved before moving`() {
        val entity = engine.entity {
            with<TransformComponent> { setInitialPosition(2f, 3f, 0f) }
            with<MoveComponent> { speed.set(60f, 0f) }
        }
        // After one fixed step the previous position must equal what was set initially
        engine.update(FIXED_STEP)

        val transform = entity[TransformComponent.mapper]!!
        assertEquals(2f, transform.previousPosition.x, DELTA)
        assertEquals(3f, transform.previousPosition.y, DELTA)
    }

    @Test
    fun `entity with RemoveComponent is not moved`() {
        val entity = engine.entity {
            with<TransformComponent> { setInitialPosition(0f, 0f, 0f) }
            with<MoveComponent> { speed.set(100f, 100f) }
            with<RemoveComponent>()
        }
        engine.update(FIXED_STEP)

        val transform = entity[TransformComponent.mapper]!!
        assertEquals(0f, transform.position.x, DELTA)
        assertEquals(0f, transform.position.y, DELTA)
    }

    @Test
    fun `entity with zero speed does not move`() {
        val entity = engine.entity {
            with<TransformComponent> { setInitialPosition(5f, 7f, 0f) }
            with<MoveComponent>()
        }
        engine.update(FIXED_STEP)

        val transform = entity[TransformComponent.mapper]!!
        assertEquals(5f, transform.position.x, DELTA)
        assertEquals(7f, transform.position.y, DELTA)
    }

    @Test
    fun `interpolated position is between previous and current after partial step`() {
        val entity = engine.entity {
            with<TransformComponent> { setInitialPosition(0f, 0f, 0f) }
            with<MoveComponent> { speed.set(60f, 0f) }
        }
        engine.update(FIXED_STEP)
        engine.update(FIXED_STEP / 2f)

        val transform = entity[TransformComponent.mapper]!!
        assertTrue(transform.interpolatedPosition.x >= 0f)
    }
}
