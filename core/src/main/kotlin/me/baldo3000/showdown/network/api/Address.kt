package me.baldo3000.showdown.network.api

import io.ktor.network.sockets.*
import io.ktor.util.network.*

data class Address(val ip: String, val port: Int) {
    companion object {
        fun anyPortAnyInterface() = Address("0.0.0.0", 0)
        fun localPortAnyInterface(port: Int) = Address("0.0.0.0", port)
        fun localhost(port: Int) = Address("127.0.0.1", port)
    }

    private var cachedInet: InetSocketAddress? = null

    val inetSocketAddress: InetSocketAddress
        get() = cachedInet ?: InetSocketAddress(ip, port).also { cachedInet = it }
}

fun SocketAddress.toAddress(): Address {
    val javaAddr = this.toJavaAddress()
    return Address(javaAddr.address, javaAddr.port)
}
