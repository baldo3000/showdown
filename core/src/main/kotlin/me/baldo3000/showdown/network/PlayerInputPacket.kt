package me.baldo3000.showdown.network

import kotlinx.serialization.Serializable
import me.baldo3000.showdown.data.Vector2D
import kotlin.uuid.Uuid

@Serializable
data class PlayerInputPacket(
    val id: Uuid,
    val horizontal: Int,
    val vertical: Int,
    val touching: Vector2D? = null,
    val sequenceNumber: Int = 0
)
