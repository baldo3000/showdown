package me.baldo3000.showdown.ecs.component

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import kotlin.uuid.Uuid

@OptIn(kotlin.uuid.ExperimentalUuidApi::class)
class IdComponentTest {

    private lateinit var idComponent: IdComponent

    @BeforeEach
    fun setUp() {
        idComponent = IdComponent()
    }

    @Test
    fun `default id is not null`() {
        assertNotNull(idComponent.id)
    }

    @Test
    fun `id can be set`() {
        val customId = Uuid.random()
        idComponent.id = customId
        assertEquals(customId, idComponent.id)
    }

    @Test
    fun `reset generates a new id`() {
        val originalId = idComponent.id
        idComponent.reset()
        assertNotEquals(originalId, idComponent.id)
    }

    @Test
    fun `two IdComponents have different default ids`() {
        val other = IdComponent()
        assertNotEquals(idComponent.id, other.id)
    }
}
