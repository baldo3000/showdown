package me.baldo3000.showdown.game

import me.baldo3000.showdown.data.Vector2D

class GameState {
    private val world: ShowdownWorld = ShowdownWorld()
    var inputEnabled: Boolean = true
    val mapSize
        get() = world.mapSize

    fun isGameFull(): Boolean = world.isGameFull()

    fun newPlayerSpawnLocation(): Vector2D = world.newPlayerSpawnLocation()

    fun reset() {
        world.reset()
        inputEnabled = true
    }
}
