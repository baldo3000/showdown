package me.baldo3000.showdown.game

import me.baldo3000.showdown.data.Vector2D
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class ShowdownWorldTest {

    @Test
    fun `new world is not full initially`() {
        val world = ShowdownWorld()
        assertFalse(world.isGameFull())
    }

    @Test
    fun `newPlayerSpawnLocation returns a location within map bounds`() {
        val world = ShowdownWorld()
        val location = world.newPlayerSpawnLocation()
        assertTrue(location.x >= 0f && location.x <= world.mapSize.x)
        assertTrue(location.y >= 0f && location.y <= world.mapSize.y)
    }

    @Test
    fun `spawn locations are unique`() {
        val world = ShowdownWorld()
        val locations = mutableSetOf<Pair<Float, Float>>()
        repeat(world.maxPlayers) {
            val loc = world.newPlayerSpawnLocation()
            locations.add(Pair(loc.x, loc.y))
        }
        assertEquals(world.maxPlayers, locations.size)
    }

    @Test
    fun `isGameFull returns true after all players spawned`() {
        val world = ShowdownWorld()
        repeat(world.maxPlayers) {
            world.newPlayerSpawnLocation()
        }
        assertTrue(world.isGameFull())
    }

    @Test
    fun `newPlayerSpawnLocation throws when game is full`() {
        val world = ShowdownWorld()
        repeat(world.maxPlayers) {
            world.newPlayerSpawnLocation()
        }
        assertThrows<IllegalStateException> {
            world.newPlayerSpawnLocation()
        }
    }

    @Test
    fun `reset allows spawning again`() {
        val world = ShowdownWorld()
        repeat(world.maxPlayers) {
            world.newPlayerSpawnLocation()
        }
        assertTrue(world.isGameFull())
        world.reset()
        assertFalse(world.isGameFull())
        val loc = world.newPlayerSpawnLocation()
        assertNotNull(loc)
    }

    @Test
    fun `all spawn locations are within bounds for custom map`() {
        val world = ShowdownWorld(mapSize = Vector2D(50f, 50f), maxPlayers = 8)
        repeat(8) {
            val loc = world.newPlayerSpawnLocation()
            assertTrue(loc.x in 0f..50f, "x=${loc.x} out of bounds")
            assertTrue(loc.y in 0f..50f, "y=${loc.y} out of bounds")
        }
    }

    @Test
    fun `world with 0 max players is immediately full`() {
        val world = ShowdownWorld(maxPlayers = 0)
        assertTrue(world.isGameFull())
    }
}
