package me.baldo3000.showdown.ecs.system

import com.badlogic.ashley.core.PooledEngine
import ktx.ashley.entity
import ktx.ashley.get
import ktx.ashley.with
import me.baldo3000.showdown.ecs.component.RemoveComponent
import me.baldo3000.showdown.ecs.component.TransformComponent
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class RemoveSystemTest {

    private lateinit var engine: PooledEngine

    @BeforeEach
    fun setUp() {
        engine = PooledEngine()
        engine.addSystem(RemoveSystem())
    }

    @Test
    fun `entity with zero delay is removed after one update`() {
        engine.entity {
            with<RemoveComponent> { delay = 0f }
        }
        assertEquals(1, engine.entities.size())
        engine.update(0.016f)
        assertEquals(0, engine.entities.size())
    }

    @Test
    fun `entity with positive delay is not removed before delay expires`() {
        engine.entity {
            with<RemoveComponent> { delay = 1f }
        }
        engine.update(0.016f)
        assertEquals(1, engine.entities.size())
    }

    @Test
    fun `delay is decremented each update`() {
        val entity = engine.entity {
            with<RemoveComponent> { delay = 1f }
        }
        engine.update(0.5f)
        val remove = entity[RemoveComponent.mapper]!!
        assertEquals(0.5f, remove.delay, 1e-4f)
    }

    @Test
    fun `entity is removed once delay runs out across multiple updates`() {
        engine.entity {
            with<RemoveComponent> { delay = 0.1f }
        }
        engine.update(0.05f)
        assertEquals(1, engine.entities.size())
        engine.update(0.06f)
        assertEquals(0, engine.entities.size())
    }

    @Test
    fun `all components are removed from entity when it is destroyed`() {
        val entity = engine.entity {
            with<RemoveComponent> { delay = 0f }
            with<TransformComponent>()
        }
        engine.update(0.016f)
        assertFalse(engine.entities.contains(entity, true))
    }
}
