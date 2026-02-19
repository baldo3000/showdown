package me.baldo3000.showdown.game

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class GameStateTest {

    private lateinit var gameState: GameState

    @BeforeEach
    fun setUp() {
        gameState = GameState()
    }

    @Test
    fun `initial input is enabled`() {
        assertTrue(gameState.inputEnabled)
    }

    @Test
    fun `game is not full initially`() {
        assertFalse(gameState.isGameFull())
    }

    @Test
    fun `newPlayerSpawnLocation returns non-null`() {
        val location = gameState.newPlayerSpawnLocation()
        assertNotNull(location)
    }

    @Test
    fun `spawning players eventually makes game full`() {
        repeat(gameState.maxPlayers) {
            gameState.newPlayerSpawnLocation()
        }
        assertTrue(gameState.isGameFull())
    }

    @Test
    fun `newPlayerSpawnLocation throws when full`() {
        repeat(10) {
            gameState.newPlayerSpawnLocation()
        }
        assertThrows<IllegalStateException> {
            gameState.newPlayerSpawnLocation()
        }
    }

    @Test
    fun `reset resets input enabled`() {
        gameState.inputEnabled = false
        gameState.reset()
        assertTrue(gameState.inputEnabled)
    }

    @Test
    fun `reset allows new spawns`() {
        repeat(10) {
            gameState.newPlayerSpawnLocation()
        }
        assertTrue(gameState.isGameFull())
        gameState.reset()
        assertFalse(gameState.isGameFull())
        assertNotNull(gameState.newPlayerSpawnLocation())
    }
}

