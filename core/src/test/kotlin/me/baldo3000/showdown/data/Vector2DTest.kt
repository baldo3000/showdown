package me.baldo3000.showdown.data

import com.badlogic.gdx.math.Vector2
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertInstanceOf
import org.junit.jupiter.api.Test

class Vector2DTest {

    @Test
    fun `default constructor creates zero vector`() {
        val v = Vector2D()
        assertEquals(0f, v.x)
        assertEquals(0f, v.y)
    }

    @Test
    fun `constructor with parameters sets x and y`() {
        val v = Vector2D(3f, 4f)
        assertEquals(3f, v.x)
        assertEquals(4f, v.y)
    }

    @Test
    fun `extends Vector2`() {
        val v = Vector2D(1f, 2f)
        assertInstanceOf(Vector2::class.java, v)
    }
}

