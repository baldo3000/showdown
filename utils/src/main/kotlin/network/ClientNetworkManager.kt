package network

import io.github.oshai.kotlinlogging.KotlinLogging
import io.ktor.network.selector.*
import io.ktor.network.sockets.*
import io.ktor.utils.io.*
import io.ktor.utils.io.core.*
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.io.readByteArray
import network.api.Client

class ClientNetworkManager : Client {
    private val logger = KotlinLogging.logger("ClientNetworkManager")
    private val selector = SelectorManager(Dispatchers.IO)
    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private val runningJobs = mutableSetOf<Job>()

    private val _sendChannel = Channel<ByteArray>(128, onBufferOverflow = BufferOverflow.DROP_OLDEST)
    private val _receiveChannel = Channel<ByteArray>(128, onBufferOverflow = BufferOverflow.DROP_OLDEST)
    val receiveChannel: ReceiveChannel<ByteArray> = _receiveChannel

    override fun connect(hostIp: String, port: Int) {
        runningJobs += scope.launch {
            var tcpSocket: Socket? = null
            var udpSocket: BoundDatagramSocket? = null
            try {
                udpSocket = aSocket(selector).udp().bind("0.0.0.0", 0)
                val localUdpPort = udpSocket.localAddress.port()
                logger.info { "Client is connected to host" }

                tcpSocket = aSocket(selector).tcp().connect(hostIp, port)
                val tcpOut = tcpSocket.openWriteChannel(autoFlush = true)
                logger.debug { "Client udp channel is connected to host" }

                // Handshake
                tcpOut.writeInt(localUdpPort)

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
                    tcpSocket.openReadChannel().readByte()
                } finally {
                    logger.info { "Connection closed from host" }
                }
            } catch (_: ClosedByteChannelException) {
//            } catch (e: Exception) {
//                logger.error(e) { "Error during peer related network operation:\n${e.message}" }
            } finally {
                logger.debug { "Closing sockets..." }
                tcpSocket?.close()
                udpSocket?.close()
                logger.debug { "Sockets closed" }
            }
        }
    }

    override fun sendToHost(payload: ByteArray) {
        _sendChannel.trySend(payload)
    }

    override fun stop() {
        logger.info { "Stopping client..." }
        scope.cancel()
        runBlocking { runningJobs.joinAll() }
        logger.info { "Client is now stopped" }
    }
}
