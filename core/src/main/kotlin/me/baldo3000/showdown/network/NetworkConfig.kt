package me.baldo3000.showdown.network

import me.baldo3000.showdown.network.api.Address

data class NetworkConfig(
    var mode: Mode = Mode.HOST,
    var hostAddress: Address = Address("127.0.0.1", 8080)
) {
    enum class Mode { HOST, CLIENT }
}
