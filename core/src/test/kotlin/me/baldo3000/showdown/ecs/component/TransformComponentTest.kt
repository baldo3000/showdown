package me.baldo3000.showdown.ecs.component

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class TransformComponentTest {

    private lateinit var transform: TransformComponent

    @BeforeEach
    fun setUp() {
        transform = TransformComponent()
    }

    @Test
    fun `default position is zero`() {
        assertEquals(0f, transform.position.x)
        assertEquals(0f, transform.position.y)
        assertEquals(0f, transform.position.z)
    }

    @Test
    fun `setInitialPosition sets position, previous and interpolated`() {
        transform.setInitialPosition(5f, 10f, 1f)
        assertEquals(5f, transform.position.x)
        assertEquals(10f, transform.position.y)
        assertEquals(1f, transform.position.z)

        assertEquals(5f, transform.previousPosition.x)
        assertEquals(10f, transform.previousPosition.y)
        assertEquals(1f, transform.previousPosition.z)

        assertEquals(5f, transform.interpolatedPosition.x)
        assertEquals(10f, transform.interpolatedPosition.y)
        assertEquals(1f, transform.interpolatedPosition.z)
    }

    @Test
    fun `default size is 1x1`() {
        assertEquals(1f, transform.size.x)
        assertEquals(1f, transform.size.y)
    }

    @Test
    fun `default rotation is zero`() {
        assertEquals(0f, transform.rotationDeg)
    }

    @Test
    fun `reset clears position and size`() {
        transform.setInitialPosition(10f, 20f, 5f)
        transform.size.set(3f, 4f)
        transform.rotationDeg = 45f
        transform.reset()

        assertEquals(0f, transform.position.x)
        assertEquals(0f, transform.position.y)
        assertEquals(0f, transform.position.z)
        assertEquals(1f, transform.size.x)
        assertEquals(1f, transform.size.y)
        assertEquals(0f, transform.rotationDeg)
    }

    @Test
    fun `compareTo sorts by z first`() {
        val a = TransformComponent().apply { setInitialPosition(0f, 10f, 1f) }
        val b = TransformComponent().apply { setInitialPosition(0f, 0f, 2f) }
        assertTrue(a < b)
    }

    @Test
    fun `compareTo sorts by y when z is equal`() {
        val a = TransformComponent().apply { setInitialPosition(0f, 5f, 0f) }
        val b = TransformComponent().apply { setInitialPosition(0f, 10f, 0f) }
        assertTrue(a < b)
    }

    @Test
    fun `compareTo returns zero for same position`() {
        val a = TransformComponent().apply { setInitialPosition(1f, 2f, 3f) }
        val b = TransformComponent().apply { setInitialPosition(1f, 2f, 3f) }
        assertEquals(0, a.compareTo(b))
    }
}
