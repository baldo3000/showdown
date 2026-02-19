package me.baldo3000.showdown.network

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertDoesNotThrow

class NetworkManagerUDPDropRateTest {

    @Test
    fun `HostNetworkManager setUDPDropRate accepts valid rate`() {
        val manager = HostNetworkManager()
        assertDoesNotThrow { manager.setUDPDropRate(0.5f) }
    }

    @Test
    fun `HostNetworkManager setUDPDropRate accepts negative values`() {
        val manager = HostNetworkManager()
        assertDoesNotThrow { manager.setUDPDropRate(-1f) }
    }

    @Test
    fun `HostNetworkManager setUDPDropRate accepts values above 1`() {
        val manager = HostNetworkManager()
        assertDoesNotThrow { manager.setUDPDropRate(2f) }
    }
}
