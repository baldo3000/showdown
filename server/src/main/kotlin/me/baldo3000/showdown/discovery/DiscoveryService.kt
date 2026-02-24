package me.baldo3000.showdown.discovery

import io.ktor.network.selector.*
import io.ktor.network.sockets.*
import io.ktor.server.application.*
import io.ktor.util.logging.*
import io.ktor.utils.io.*
import io.ktor.utils.io.core.ByteReadPacket
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlin.text.toByteArray

const val DISCOVERY_PORT = 8081
const val DISCOVERY_REQUEST = "hello server"
const val DISCOVERY_RESPONSE = "hello client"

fun Application.configureDiscovery() {
    launch(Dispatchers.IO) {
        val socket = aSocket(SelectorManager(Dispatchers.IO)).udp().bind(port = DISCOVERY_PORT) //{ broadcast = true }
        monitor.subscribe(ApplicationStopped) { socket.close() }
        log.info("UDP discovery listener started on port $DISCOVERY_PORT")

        try {
            while (isActive) {
                val datagram = socket.receive()
                val message = datagram.packet.readText().trim()
                log.debug { "Received UDP message: '$message' from ${datagram.address}" }

                if (message == DISCOVERY_REQUEST) {
                    log.debug { "Discovery request from ${datagram.address}" }
                    socket.send(
                        Datagram(
                            packet = ByteReadPacket(DISCOVERY_RESPONSE.toByteArray()),
                            address = datagram.address
                        )
                    )
                }
            }
        } catch (_: kotlinx.coroutines.CancellationException) { // normal shutdown
        } catch (_: Exception) {
        } finally {
            socket.close()
        }

        log.debug { "UDP discovery listener stopped" }
    }
}
