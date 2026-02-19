package me.baldo3000.showdown.network

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class NetworkConfigTest {

    @Test
    fun `default mode is HOST`() {
        val config = NetworkConfig()
        assertEquals(NetworkConfig.Mode.HOST, config.mode)
    }

    @Test
    fun `default host address is localhost 8080`() {
        val config = NetworkConfig()
        assertEquals("127.0.0.1", config.hostAddress.ip)
        assertEquals(8080, config.hostAddress.port)
    }

    @Test
    fun `default udp drop rate is zero`() {
        val config = NetworkConfig()
        assertEquals(0f, config.udpDropRate)
    }
}

