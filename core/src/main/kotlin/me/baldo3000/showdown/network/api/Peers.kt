package me.baldo3000.showdown.network.api

import io.ktor.network.sockets.*
import kotlin.uuid.Uuid

data class ConnectedPeer(
    val tcpSocket: Socket,
    val udpAddress: Address
)

interface Host {
    fun start(port: Int = 0)

    fun sendToClients(payload: ByteArray)

    fun disconnectClient(peerId: Uuid)

    fun stop()
}

interface Client {
    fun connect(hostIp: String, port: Int)

    fun sendToHost(payload: ByteArray)

    fun stop()
}
