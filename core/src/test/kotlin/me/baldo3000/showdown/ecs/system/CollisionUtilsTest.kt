package me.baldo3000.showdown.ecs.system

import com.badlogic.gdx.math.Circle
import com.badlogic.gdx.math.Rectangle
import com.badlogic.gdx.math.Shape2D
import com.badlogic.gdx.math.Vector2
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class CollisionUtilsTest {

    @Test
    fun `setCenter on Circle moves center`() {
        val circle = Circle(0f, 0f, 5f)
        (circle as Shape2D).setCenter(10f, 20f)
        assertEquals(10f, circle.x)
        assertEquals(20f, circle.y)
    }

    @Test
    fun `setCenter on Rectangle moves center`() {
        val rect = Rectangle(0f, 0f, 10f, 8f)
        (rect as Shape2D).setCenter(15f, 25f)
        assertEquals(15f, rect.x + rect.width / 2f)
        assertEquals(25f, rect.y + rect.height / 2f)
    }

    @Test
    fun `setCenter on unsupported shape throws`() {
        val unsupported = object : Shape2D {
            override fun contains(x: Float, y: Float): Boolean = false
            override fun contains(point: Vector2): Boolean = false
        }
        assertThrows<UnsupportedOperationException> {
            unsupported.setCenter(0f, 0f)
        }
    }

    @Test
    fun `circle overlaps circle - overlapping`() {
        val a = Circle(0f, 0f, 5f)
        val b = Circle(3f, 0f, 5f)
        assertTrue((a as Shape2D).overlaps(b as Shape2D))
    }

    @Test
    fun `circle overlaps circle - not overlapping`() {
        val a = Circle(0f, 0f, 1f)
        val b = Circle(100f, 100f, 1f)
        assertFalse((a as Shape2D).overlaps(b as Shape2D))
    }

    @Test
    fun `circle overlaps rectangle - overlapping`() {
        val circle = Circle(5f, 5f, 3f)
        val rect = Rectangle(4f, 4f, 4f, 4f)
        assertTrue((circle as Shape2D).overlaps(rect as Shape2D))
    }

    @Test
    fun `circle overlaps rectangle - not overlapping`() {
        val circle = Circle(0f, 0f, 1f)
        val rect = Rectangle(100f, 100f, 2f, 2f)
        assertFalse((circle as Shape2D).overlaps(rect as Shape2D))
    }

    @Test
    fun `rectangle overlaps circle - overlapping`() {
        val rect = Rectangle(4f, 4f, 4f, 4f)
        val circle = Circle(5f, 5f, 3f)
        assertTrue((rect as Shape2D).overlaps(circle as Shape2D))
    }

    @Test
    fun `rectangle overlaps circle - not overlapping`() {
        val rect = Rectangle(0f, 0f, 1f, 1f)
        val circle = Circle(100f, 100f, 1f)
        assertFalse((rect as Shape2D).overlaps(circle as Shape2D))
    }

    @Test
    fun `rectangle overlaps rectangle - overlapping`() {
        val a = Rectangle(0f, 0f, 5f, 5f)
        val b = Rectangle(3f, 3f, 5f, 5f)
        assertTrue((a as Shape2D).overlaps(b as Shape2D))
    }

    @Test
    fun `rectangle overlaps rectangle - not overlapping`() {
        val a = Rectangle(0f, 0f, 1f, 1f)
        val b = Rectangle(10f, 10f, 1f, 1f)
        assertFalse((a as Shape2D).overlaps(b as Shape2D))
    }

    @Test
    fun `overlaps with unsupported outer shape throws`() {
        val unsupported = object : Shape2D {
            override fun contains(x: Float, y: Float): Boolean = false
            override fun contains(point: Vector2): Boolean = false
        }
        val circle = Circle(0f, 0f, 1f)
        assertThrows<UnsupportedOperationException> {
            unsupported.overlaps(circle as Shape2D)
        }
    }

    @Test
    fun `overlaps with unsupported inner shape throws`() {
        val circle = Circle(0f, 0f, 1f)
        val unsupported = object : Shape2D {
            override fun contains(x: Float, y: Float): Boolean = false
            override fun contains(point: Vector2): Boolean = false
        }
        assertThrows<UnsupportedOperationException> {
            (circle as Shape2D).overlaps(unsupported)
        }
    }

    @Test
    fun `overlaps is symmetric for circle-rectangle`() {
        val circle = Circle(5f, 5f, 2f)
        val rect = Rectangle(4f, 4f, 3f, 3f)
        val circleOverlapsRect = (circle as Shape2D).overlaps(rect as Shape2D)
        val rectOverlapsCircle = (rect as Shape2D).overlaps(circle as Shape2D)
        assertEquals(circleOverlapsRect, rectOverlapsCircle)
    }
}

