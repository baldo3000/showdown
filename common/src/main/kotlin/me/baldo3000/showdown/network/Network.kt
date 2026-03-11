package me.baldo3000.showdown.network

import java.net.Inet4Address
import java.net.NetworkInterface
import java.util.*

const val LOBBIES_ROUTE = "/lobbies"
const val LOBBY_ROUTE = "/lobby"
const val DISCOVERY_PORT = 8081
const val DISCOVERY_REQUEST = "Lobby server where are you?"

fun localIpv4Addresses(): List<String> {
    val skipKeywords = listOf("vEthernet", "WSL", "Hyper-V")

    fun isFaceAllowed(netIf: NetworkInterface): Boolean {
        val name = netIf.name ?: ""
        val display = netIf.displayName ?: ""
        return skipKeywords.none { kw ->
            name.contains(kw, ignoreCase = true) || display.contains(
                kw,
                ignoreCase = true
            )
        }
    }

    return Collections.list(NetworkInterface.getNetworkInterfaces())
        .asSequence()
        .filter { it.isUp && isFaceAllowed(it) && !it.isLoopback }
        .flatMap { Collections.list(it.inetAddresses).asSequence() }
        .filterIsInstance<Inet4Address>()
        .filter { !it.isLinkLocalAddress && !it.isLoopbackAddress }
        .map { it.hostAddress }
        .toList()
}
