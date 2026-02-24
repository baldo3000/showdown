package me.baldo3000.showdown.lobbies

import java.util.*

data class Lobby(val ip: String, val port: Int, val dateAdded: Long = System.currentTimeMillis()) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Lobby) return false
        return ip == other.ip && port == other.port
    }

    override fun hashCode(): Int {
        return Objects.hash(ip, port)
    }
}
