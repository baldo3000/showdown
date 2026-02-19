package me.baldo3000.showdown.ecs.component

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class HealthComponentTest {

    private lateinit var health: HealthComponent

    @BeforeEach
    fun setUp() {
        health = HealthComponent()
    }

    @Test
    fun `default health is max`() {
        assertEquals(MAX_HEALTH, health.health)
    }

    @Test
    fun `health can be reduced`() {
        health.health -= 25f
        assertEquals(75f, health.health)
    }

    @Test
    fun `reset restores health to max`() {
        health.health = 0f
        health.reset()
        assertEquals(MAX_HEALTH, health.health)
    }
}

