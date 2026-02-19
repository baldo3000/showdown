package me.baldo3000.showdown.ecs.component

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class RemoveComponentTest {

    private lateinit var remove: RemoveComponent

    @BeforeEach
    fun setUp() {
        remove = RemoveComponent()
    }

    @Test
    fun `default delay is zero`() {
        assertEquals(0f, remove.delay)
    }

    @Test
    fun `delay can be set`() {
        remove.delay = 2.5f
        assertEquals(2.5f, remove.delay)
    }

    @Test
    fun `reset clears delay to zero`() {
        remove.delay = 5f
        remove.reset()
        assertEquals(0f, remove.delay)
    }
}
