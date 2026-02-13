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
import network.api.Address
import network.api.ConnectedPeer
import network.api.Host
import network.api.toAddress
import java.util.concurrent.ConcurrentHashMap
import kotlin.uuid.Uuid

class HostNetworkManager(
    val onPeerConnect: (Uuid) -> Unit = {},
    val onPeerDisconnect: (Uuid) -> Unit = {}
) : Host {
    private val logger = KotlinLogging.logger("HostNetworkManager")
    private val selector = SelectorManager(Dispatchers.IO)
    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private val runningJobs = mutableSetOf<Job>()

    private val connectedPeers = ConcurrentHashMap<Uuid, ConnectedPeer>()
    private val tcpOuts = ConcurrentHashMap<Uuid, ByteWriteChannel>()

    private val _sendChannel = Channel<ByteArray>(128, onBufferOverflow = BufferOverflow.DROP_OLDEST)
    private val _receiveChannel = Channel<ByteArray>(128, onBufferOverflow = BufferOverflow.DROP_OLDEST)
    val receiveChannel: ReceiveChannel<ByteArray> = _receiveChannel

    val connectedPeerIds: Set<Uuid>
        get() = connectedPeers.keys

    override fun start(port: Int) {
        runningJobs += scope.launch {
            var tcpServer: ServerSocket? = null
            var udpSocket: BoundDatagramSocket? = null
            try {
                tcpServer = aSocket(selector).tcp().bind("0.0.0.0", port)
                udpSocket = aSocket(selector).udp().bind("0.0.0.0", port)

                logger.info { "Host is listening on ${tcpServer.localAddress}" }

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
                    for (payload in _sendChannel) {
                        // logger.info { "Broadcasting UDP message: ${payload.decodeToString()}" }
                        connectedPeers.values.forEach { peer ->
                            udpSocket.send(
                                Datagram(
                                    buildPacket { writeFully(payload) },
                                    peer.udpAddress.inetSocketAddress
                                )
                            )
                        }
                    }
                }

                // TCP Connection Acceptor
                while (isActive) {
                    val socket = tcpServer.accept()
                    handleNewConnection(socket)
                }
            } catch (_: ClosedByteChannelException) {
//            } catch (e: Exception) {
//                logger.error(e) { "Error during host related network operation:\n${e.message}" }
            } finally {
                logger.debug { "Closing host sockets..." }
                tcpServer?.close()
                udpSocket?.close()
                logger.debug { "Host sockets closed" }
            }
        }
    }

    override fun sendToClients(payload: ByteArray) {
        _sendChannel.trySend(payload)
    }

    override fun stop() {
        logger.info { "Stopping host..." }
        //scope.cancel()
        runBlocking { runningJobs.forEach { it.cancelAndJoin() } }
        logger.info { "Host is now stopped" }
        connectedPeers.clear()
        tcpOuts.clear()
        runningJobs.clear()
    }

    private fun handleNewConnection(socket: Socket) {
        runningJobs += scope.launch {
            val peerId = Uuid.random()
            val input = socket.openReadChannel()
            val output = socket.openWriteChannel(autoFlush = true)

            try {
                // Handshake: Get their UDP port
                val udpPort = input.readInt()
                logger.info { "Received client udp port: $udpPort" }
                output.writeByteArray(peerId.toByteArray())

                val remoteIp = socket.remoteAddress.toAddress()
                val udpAddress = Address(remoteIp.ip, udpPort)
                logger.info { "Peer $peerId at $udpAddress connected" }

                connectedPeers[peerId] = ConnectedPeer(socket, udpAddress)
                tcpOuts[peerId] = output
                onPeerConnect(peerId)

                // Keep-alive loop
                while (!socket.isClosed) {
                    input.readByte()
                }
            } catch (e: Exception) {
                // Disconnection happening
                logger.info { "Connection aborted with $peerId at ${connectedPeers[peerId]} connected" }
            } finally {
                onPeerDisconnect(peerId)
                tcpOuts.remove(peerId)
                connectedPeers.keys.removeIf { it == peerId }
                socket.close()
            }
        }
    }
}
