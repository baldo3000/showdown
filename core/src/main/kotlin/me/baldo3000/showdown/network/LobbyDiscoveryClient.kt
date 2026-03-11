package me.baldo3000.showdown.network

import io.ktor.network.selector.*
import io.ktor.network.sockets.*
import io.ktor.utils.io.core.*
import kotlinx.coroutines.*
import kotlinx.serialization.json.Json
import ktx.log.logger
import me.baldo3000.showdown.dto.AddressDTO

private const val DISCOVERY_TIMEOUT_MS = 3000L
private const val BROADCAST_ADDRESS = "255.255.255.255"

class LobbyDiscoveryClient {
    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private var searchJob: Job? = null

    fun search(
        onResult: (AddressDTO) -> Unit,
        onTimeout: () -> Unit = {},
    ) {
        searchJob?.cancel()
        searchJob = scope.launch {
            val selector = SelectorManager(Dispatchers.IO)
            try {
                val socket = aSocket(selector).udp().bind(hostname = "0.0.0.0", port = 0) {
                    broadcast = true
                }
                try {
                    val requestBytes = DISCOVERY_REQUEST.toByteArray()
                    socket.send(
                        Datagram(
                            packet = buildPacket { writeFully(requestBytes) },
                            address = InetSocketAddress(BROADCAST_ADDRESS, DISCOVERY_PORT)
                        )
                    )
                    log.debug { "Discovery broadcast sent to $BROADCAST_ADDRESS:$DISCOVERY_PORT" }

                    val addressDTO = withTimeoutOrNull(DISCOVERY_TIMEOUT_MS) {
                        val datagram = socket.receive()
                        val response = datagram.packet.readText().trim()
                        log.debug { "Discovery response received: $response" }
                        Json.decodeFromString<AddressDTO>(response)
                    }

                    if (addressDTO != null) {
                        onResult(addressDTO)
                    } else {
                        log.debug { "Discovery timed out after ${DISCOVERY_TIMEOUT_MS}ms" }
                        onTimeout()
                    }
                } finally {
                    socket.close()
                }
            } catch (e: CancellationException) {
                throw e // always rethrow so structured concurrency works correctly
            } catch (e: Exception) {
                log.error { "Discovery error: ${e.message}" }
            } finally {
                selector.close()
            }
        }
    }

    fun dispose() {
        scope.cancel()
    }

    companion object {
        private val log = logger<LobbyDiscoveryClient>()
    }
}

