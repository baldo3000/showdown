package me.baldo3000.showdown.data

import com.badlogic.gdx.math.Vector3
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertInstanceOf
import org.junit.jupiter.api.Test

class Vector3DTest {

    @Test
    fun `default constructor creates zero vector`() {
        val v = Vector3D()
        assertEquals(0f, v.x)
        assertEquals(0f, v.y)
        assertEquals(0f, v.z)
    }

    @Test
    fun `constructor with parameters sets x, y and z`() {
        val v = Vector3D(1f, 2f, 3f)
        assertEquals(1f, v.x)
        assertEquals(2f, v.y)
        assertEquals(3f, v.z)
    }

    @Test
    fun `extends Vector3`() {
        val v = Vector3D(1f, 2f, 3f)
        assertInstanceOf(Vector3::class.java, v)
    }
}

