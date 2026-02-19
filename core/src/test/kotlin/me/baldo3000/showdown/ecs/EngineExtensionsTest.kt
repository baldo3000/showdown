package me.baldo3000.showdown.ecs

import com.badlogic.ashley.core.PooledEngine
import ktx.ashley.entity
import ktx.ashley.with
import me.baldo3000.showdown.ecs.component.*
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class EngineExtensionsTest {

    private lateinit var engine: PooledEngine

    @BeforeEach
    fun setUp() {
        engine = PooledEngine()
    }

    @Test
    fun `players returns empty when no entities`() {
        assertTrue(engine.players.isEmpty())
    }

    @Test
    fun `players returns player entities`() {
        engine.entity {
            with<IdComponent>()
            with<HealthComponent>()
            with<TransformComponent>()
            with<MoveComponent>()
            with<ColliderComponent>()
            with<GraphicComponent>()
        }
        assertEquals(1, engine.players.size)
    }

    @Test
    fun `players excludes entities with RemoveComponent`() {
        engine.entity {
            with<IdComponent>()
            with<HealthComponent>()
            with<TransformComponent>()
            with<MoveComponent>()
            with<ColliderComponent>()
            with<GraphicComponent>()
            with<RemoveComponent>()
        }
        assertTrue(engine.players.isEmpty())
    }

    @Test
    fun `character returns null when no controllable entity`() {
        assertNull(engine.character)
    }

    @Test
    fun `character returns entity with InputComponent`() {
        engine.entity {
            with<InputComponent>()
        }
        assertNotNull(engine.character)
    }

    @Test
    fun `character excludes entities with RemoveComponent`() {
        engine.entity {
            with<InputComponent>()
            with<RemoveComponent>()
        }
        assertNull(engine.character)
    }

    @Test
    fun `bullets returns empty when no bullet entities`() {
        assertTrue(engine.bullets.isEmpty())
    }

    @Test
    fun `bullets returns entities with bullet components`() {
        engine.entity {
            with<IdComponent>()
            with<DamageComponent>()
            with<TransformComponent>()
            with<MoveComponent>()
            with<ColliderComponent>()
            with<GraphicComponent>()
        }
        assertEquals(1, engine.bullets.size)
    }

    @Test
    fun `bullets excludes entities with RemoveComponent`() {
        engine.entity {
            with<IdComponent>()
            with<DamageComponent>()
            with<TransformComponent>()
            with<MoveComponent>()
            with<ColliderComponent>()
            with<GraphicComponent>()
            with<RemoveComponent>()
        }
        assertTrue(engine.bullets.isEmpty())
    }

    @Test
    fun `walls returns empty when no wall entities`() {
        assertTrue(engine.walls.isEmpty())
    }

    @Test
    fun `walls returns entities with wall components`() {
        engine.entity {
            with<IdComponent>()
            with<TransformComponent>()
            with<ColliderComponent>()
            with<GraphicComponent>()
        }
        assertEquals(1, engine.walls.size)
    }

    @Test
    fun `walls excludes entities with RemoveComponent`() {
        engine.entity {
            with<IdComponent>()
            with<TransformComponent>()
            with<ColliderComponent>()
            with<GraphicComponent>()
            with<RemoveComponent>()
        }
        assertTrue(engine.walls.isEmpty())
    }
}

