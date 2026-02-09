package me.baldo3000.showdown.event.api

import kotlinx.serialization.Serializable
import me.baldo3000.showdown.data.Vector2D
import kotlin.uuid.Uuid

@Serializable
sealed interface GameEvent {
    @Serializable
    data class PlayerJoin(val id: Uuid) : GameEvent

    @Serializable
    data class PlayerQuit(val id: Uuid) : GameEvent

    @Serializable
    data class PlayerDeath(val id: Uuid) : GameEvent

    @Serializable
    data class PlayerShoot(val id: Uuid, val direction: Vector2D) : GameEvent
}
