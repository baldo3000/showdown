package me.baldo3000.showdown.world

import kotlin.uuid.Uuid

interface World {
    fun init()

    fun isGameFull(): Boolean

    fun spawnNewPlayer(id: Uuid)
    fun removePlayer(id: Uuid)
}
