package me.baldo3000.showdown.network

import io.ktor.network.selector.*
import io.ktor.network.sockets.*
import io.ktor.utils.io.*
import io.ktor.utils.io.core.*
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.io.readByteArray
import ktx.log.logger
import me.baldo3000.showdown.network.api.Client
import kotlin.concurrent.atomics.AtomicReference
import kotlin.uuid.Uuid

class ClientNetworkManager(
    val onConnect: (Uuid) -> Unit = {},
    val onDisconnect: () -> Unit = {}
) : Client {
    private val selector = SelectorManager(Dispatchers.IO)
    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private val runningJobs = mutableSetOf<Job>()

    private val _sendChannel = Channel<ByteArray>(128, onBufferOverflow = BufferOverflow.DROP_OLDEST)
    private val _receiveChannel = Channel<ByteArray>(128, onBufferOverflow = BufferOverflow.DROP_OLDEST)
    val receiveChannel: ReceiveChannel<ByteArray> = _receiveChannel

    private val _id = AtomicReference<Uuid?>(null)
    val id: Uuid?
        get() = _id.load()

    override fun connect(hostIp: String, port: Int) {
        runningJobs += scope.launch {
            var tcpSocket: Socket? = null
            var udpSocket: BoundDatagramSocket? = null
            try {
                udpSocket = aSocket(selector).udp().bind("0.0.0.0", 0)
                val localUdpPort = udpSocket.localAddress.port()
                log.info { "Client is connected to host" }

                tcpSocket = aSocket(selector).tcp().connect(hostIp, port)
                val tcpOut = tcpSocket.openWriteChannel(autoFlush = true)
                val tcpIn = tcpSocket.openReadChannel()
                log.debug { "Client udp channel is connected to host" }

                // Handshake
                tcpOut.writeInt(localUdpPort)
                val id = Uuid.fromByteArray(tcpIn.readByteArray(16))
                _id.store(id)
                onConnect(id)
                log.debug { "Received id from host: $id" }

                // UDP Listener
                launch {
                    while (isActive) {
                        val datagram = udpSocket.receive()
                        val payload = datagram.packet.readByteArray()
                        // logger.info { "Received UDP message: ${payload.decodeToString()}" }
                        _receiveChannel.trySend(payload)
                    }
                }

                // UDP Sender
                launch {
                    val hostAddress = InetSocketAddress(hostIp, port)
                    for (payload in _sendChannel) {
                        // logger.info { "Sending UDP message: ${payload.decodeToString()}" }
                        udpSocket.send(Datagram(buildPacket { writeFully(payload) }, hostAddress))
                    }
                }

                // Wait for Host to close connection
                try {
                    tcpIn.readByte()
                } finally {
                    log.info { "Connection closed from host" }
                }
            } catch (_: ClosedByteChannelException) {
//            } catch (e: Exception) {
//                log.error(e) { "Error during peer related network operation:\n${e.message}" }
            } finally {
                log.debug { "Closing sockets..." }
                tcpSocket?.close()
                udpSocket?.close()
                log.debug { "Sockets closed" }
            }
        }
    }

    override fun sendToHost(payload: ByteArray) {
        _sendChannel.trySend(payload)
    }

    override fun stop() {
        log.debug { "Stopping client..." }
        // scope.cancel()
        runBlocking { runningJobs.forEach { it.cancelAndJoin() } }
        log.debug { "Client is now stopped" }
        _id.store(null)
    }

    companion object {
        private val log = logger<ClientNetworkManager>()
    }
}
