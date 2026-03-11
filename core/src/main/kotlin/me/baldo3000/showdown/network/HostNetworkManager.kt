package me.baldo3000.showdown.network

import io.ktor.network.selector.*
import io.ktor.network.sockets.*
import io.ktor.utils.io.*
import io.ktor.utils.io.core.*
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.io.EOFException
import kotlinx.io.readByteArray
import ktx.log.logger
import me.baldo3000.showdown.network.api.Address
import me.baldo3000.showdown.network.api.ConnectedPeer
import me.baldo3000.showdown.network.api.Host
import me.baldo3000.showdown.network.api.toAddress
import java.util.concurrent.ConcurrentHashMap
import kotlin.concurrent.atomics.AtomicReference
import kotlin.uuid.Uuid

class HostNetworkManager(
    val onPeerConnect: (Uuid) -> Unit = {},
    val onPeerDisconnect: (Uuid) -> Unit = {}
) : Host {
    private var udpDropRate = 0f

    private val selector = SelectorManager(Dispatchers.IO)
    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private val runningJobs = mutableSetOf<Job>()

    private val connectedPeers = ConcurrentHashMap<Uuid, ConnectedPeer>()
    private val tcpOuts = ConcurrentHashMap<Uuid, ByteWriteChannel>()

    private val _sendChannel = Channel<ByteArray>(128, onBufferOverflow = BufferOverflow.DROP_OLDEST)
    private val _receiveChannel = Channel<ByteArray>(128, onBufferOverflow = BufferOverflow.DROP_OLDEST)
    val receiveChannel: ReceiveChannel<ByteArray> = _receiveChannel

    private val _addresses = AtomicReference<List<String>>(listOf())
    val addresses: List<String>
        get() = _addresses.load()

    private val _port = AtomicReference<Int?>(null)
    val port: Int?
        get() = _port.load()

    val connectedPeerIds: Set<Uuid>
        get() = connectedPeers.keys

    override fun start(port: Int) {
        runningJobs += scope.launch {
            var tcpServer: ServerSocket? = null
            var udpSocket: BoundDatagramSocket? = null
            try {
                tcpServer = aSocket(selector).tcp().bind("0.0.0.0", port)
                udpSocket = aSocket(selector).udp().bind("0.0.0.0", tcpServer.localAddress.port())

                // log.info { "Host is listening on ${tcpServer.localAddress}" }
                val actualAddresses = localIpv4Addresses()
                val actualPort = tcpServer.localAddress.port()
                log.info { "Host is listening on addresses $actualAddresses on port $actualPort" }
                _addresses.store(actualAddresses)
                _port.store(actualPort)

                // UDP Listener
                launch {
                    while (isActive) {
                        val datagram = udpSocket.receive()
                        val payload = datagram.packet.readByteArray()
                        // log.info { "Received UDP message: ${payload.decodeToString()}" }
                        _receiveChannel.trySend(payload)
                    }
                }

                // UDP Sender
                launch {
                    for (payload in _sendChannel) {
                        // log.info { "Broadcasting UDP message: ${payload.decodeToString()}" }
                        connectedPeers.values.forEach { peer ->
                            if (!shouldDropPacket()) {
                                udpSocket.send(
                                    Datagram(
                                        buildPacket { writeFully(payload) },
                                        peer.udpAddress.inetSocketAddress
                                    )
                                )
                            } else {
                                log.debug { "Dropping UDP packet before sending" }
                            }
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
//                log.error(e) { "Error during host related network operation:\n${e.message}" }
            } finally {
                log.debug { "Closing host sockets..." }
                tcpServer?.close()
                udpSocket?.close()
                log.debug { "Host sockets closed" }
            }
        }
    }

    override fun sendToClients(payload: ByteArray) {
        _sendChannel.trySend(payload)
    }

    override fun disconnectClient(peerId: Uuid) {
        connectedPeers[peerId]?.tcpSocket?.close()
    }

    override fun setUDPDropRate(dropRate: Float) {
        udpDropRate = dropRate.coerceIn(0f, 1f)
    }

    override fun stop() {
        log.debug { "Stopping host..." }
        //  scope.cancel()
        runBlocking { runningJobs.forEach { it.cancelAndJoin() } }
        // Discard any leftover packets
        while (_receiveChannel.tryReceive().isSuccess) { /* discard */
        }
        while (_sendChannel.tryReceive().isSuccess) {/* discard */
        }
        log.debug { "Host is now stopped" }
        connectedPeers.clear()
        tcpOuts.clear()
        runningJobs.clear()
        _addresses.store(listOf())
        _port.store(null)
    }

    private fun shouldDropPacket(): Boolean {
        return Math.random() < udpDropRate
    }

    private fun handleNewConnection(socket: Socket) {
        runningJobs += scope.launch {
            val peerId = Uuid.random()
            val input = socket.openReadChannel()
            val output = socket.openWriteChannel(autoFlush = true)

            try {
                // Handshake: Get their UDP port
                val udpPort = input.readInt()
                log.debug { "Received client udp port: $udpPort" }
                output.writeByteArray(peerId.toByteArray())

                val remoteIp = socket.remoteAddress.toAddress()
                val udpAddress = Address(remoteIp.ip, udpPort)
                log.info { "Peer $peerId at $udpAddress connected" }

                connectedPeers[peerId] = ConnectedPeer(socket, udpAddress)
                tcpOuts[peerId] = output
                onPeerConnect(peerId)

                // Keep-alive loop
                while (!socket.isClosed) {
                    input.readByte()
                }
            } catch (_: ClosedByteChannelException) { // Client crashes
                log.info { "Connection aborted with $peerId at ${connectedPeers[peerId]} connected" }
            } catch (_: EOFException) { // Client disconnects
                log.info { "Connection closed with $peerId at ${connectedPeers[peerId]} connected" }
            } finally {
                onPeerDisconnect(peerId)
                tcpOuts.remove(peerId)
                connectedPeers.keys.removeIf { it == peerId }
                socket.close()
            }
        }
    }

    companion object {
        private val log = logger<HostNetworkManager>()
    }
}
