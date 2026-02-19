package me.baldo3000.showdown.ecs.component

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class MoveComponentTest {

    private lateinit var move: MoveComponent

    @BeforeEach
    fun setUp() {
        move = MoveComponent()
    }

    @Test
    fun `default speed is zero`() {
        assertEquals(0f, move.speed.x)
        assertEquals(0f, move.speed.y)
    }

    @Test
    fun `speed can be changed`() {
        move.speed.set(3f, 5f)
        assertEquals(3f, move.speed.x)
        assertEquals(5f, move.speed.y)
    }

    @Test
    fun `reset clears speed to zero`() {
        move.speed.set(10f, 20f)
        move.reset()
        assertEquals(0f, move.speed.x)
        assertEquals(0f, move.speed.y)
    }
}
