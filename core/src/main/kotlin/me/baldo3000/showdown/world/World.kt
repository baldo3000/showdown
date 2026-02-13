package me.baldo3000.showdown.world

import me.baldo3000.showdown.data.Vector2D

interface World {
    fun isGameFull(): Boolean

    fun newPlayerSpawnLocation(): Vector2D

    fun reset()
}
