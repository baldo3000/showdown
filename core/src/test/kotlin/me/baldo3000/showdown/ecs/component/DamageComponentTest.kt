package me.baldo3000.showdown.ecs.component

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import kotlin.uuid.Uuid

@OptIn(kotlin.uuid.ExperimentalUuidApi::class)
class DamageComponentTest {

    private lateinit var damage: DamageComponent

    @BeforeEach
    fun setUp() {
        damage = DamageComponent()
    }

    @Test
    fun `default damage is DEFAULT_DAMAGE`() {
        assertEquals(DEFAULT_DAMAGE, damage.damage)
    }

    @Test
    fun `default sourceId is null`() {
        assertNull(damage.sourceId)
    }

    @Test
    fun `damage can be changed`() {
        damage.damage = 20f
        assertEquals(20f, damage.damage)
    }

    @Test
    fun `sourceId can be set`() {
        val id = Uuid.random()
        damage.sourceId = id
        assertEquals(id, damage.sourceId)
    }

    @Test
    fun `reset restores defaults`() {
        damage.damage = 99f
        damage.sourceId = Uuid.random()
        damage.reset()
        assertEquals(DEFAULT_DAMAGE, damage.damage)
        assertNull(damage.sourceId)
    }
}

