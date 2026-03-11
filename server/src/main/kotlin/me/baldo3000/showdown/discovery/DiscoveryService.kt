package me.baldo3000.showdown.discovery

import io.ktor.network.selector.*
import io.ktor.network.sockets.*
import io.ktor.server.application.*
import io.ktor.util.logging.*
import io.ktor.util.network.*
import io.ktor.utils.io.*
import io.ktor.utils.io.core.buildPacket
import io.ktor.utils.io.core.writeFully
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import me.baldo3000.showdown.dto.AddressDTO
import me.baldo3000.showdown.network.DISCOVERY_PORT
import me.baldo3000.showdown.network.DISCOVERY_REQUEST
import me.baldo3000.showdown.network.localIpv4Addresses
import kotlin.text.toByteArray


fun Application.configureDiscovery() {
    launch(Dispatchers.IO) {
        val socket = aSocket(SelectorManager(Dispatchers.IO)).udp().bind(port = DISCOVERY_PORT) { broadcast = true }
        val routingAddress = AddressDTO(localIpv4Addresses().first(), 8080)
        monitor.subscribe(ApplicationStopped) { socket.close() }
        log.debug { "UDP discovery listener started on port ${DISCOVERY_PORT}" }

        try {
            while (isActive) {
                val datagram = socket.receive()
                val message = datagram.packet.readText().trim()
                val address = datagram.address.toJavaAddress().address
                val port = datagram.address.port()
                log.debug { "Received UDP message: \"$message\" from $address:$port" }

                if (message == DISCOVERY_REQUEST) {
                    log.debug { "Discovery request from $address:$port" }
                    log.debug { "Sending that I'm here: $routingAddress" }
                    socket.send(
                        Datagram(
                            packet = buildPacket { writeFully(Json.encodeToString(routingAddress).toByteArray()) },
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
