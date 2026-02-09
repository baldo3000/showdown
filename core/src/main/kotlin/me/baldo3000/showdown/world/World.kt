package me.baldo3000.showdown.world

import me.baldo3000.showdown.network.Vector2D

interface World {
    fun isGameFull(): Boolean

    fun newPLayerSpawnLocation(): Vector2D
}
