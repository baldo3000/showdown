package me.baldo3000.showdown.ecs.component

import com.badlogic.gdx.math.Circle
import com.badlogic.gdx.math.Rectangle
import org.junit.jupiter.api.Assertions.assertInstanceOf
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class ColliderComponentTest {

    private lateinit var collider: ColliderComponent

    @BeforeEach
    fun setUp() {
        collider = ColliderComponent()
    }

    @Test
    fun `default collider is a Circle`() {
        assertInstanceOf(Circle::class.java, collider.collider)
    }

    @Test
    fun `collider can be set to Rectangle`() {
        collider.collider = Rectangle(0f, 0f, 10f, 5f)
        assertInstanceOf(Rectangle::class.java, collider.collider)
    }

    @Test
    fun `reset restores default Circle`() {
        collider.collider = Rectangle(0f, 0f, 10f, 5f)
        collider.reset()
        assertInstanceOf(Circle::class.java, collider.collider)
    }
}

