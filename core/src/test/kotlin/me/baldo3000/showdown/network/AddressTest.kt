package me.baldo3000.showdown.network

import me.baldo3000.showdown.network.api.Address
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

class AddressTest {

    @Test
    fun `constructor sets ip and port`() {
        val addr = Address("10.0.0.1", 1234)
        assertEquals("10.0.0.1", addr.ip)
        assertEquals(1234, addr.port)
    }

    @Test
    fun `anyPortAnyInterface returns 0-0-0-0 port 0`() {
        val addr = Address.anyPortAnyInterface()
        assertEquals("0.0.0.0", addr.ip)
        assertEquals(0, addr.port)
    }

    @Test
    fun `localPortAnyInterface sets correct port`() {
        val addr = Address.localPortAnyInterface(5555)
        assertEquals("0.0.0.0", addr.ip)
        assertEquals(5555, addr.port)
    }

    @Test
    fun `localhost sets correct port`() {
        val addr = Address.localhost(8080)
        assertEquals("127.0.0.1", addr.ip)
        assertEquals(8080, addr.port)
    }

    @Test
    fun `data class equality`() {
        val a = Address("127.0.0.1", 8080)
        val b = Address("127.0.0.1", 8080)
        assertEquals(a, b)
    }

    @Test
    fun `data class inequality on different port`() {
        val a = Address("127.0.0.1", 8080)
        val b = Address("127.0.0.1", 9090)
        assertNotEquals(a, b)
    }

    @Test
    fun `data class inequality on different ip`() {
        val a = Address("127.0.0.1", 8080)
        val b = Address("192.168.0.1", 8080)
        assertNotEquals(a, b)
    }

    @Test
    fun `inetSocketAddress returns correct values`() {
        val addr = Address("127.0.0.1", 8080)
        val inet = addr.inetSocketAddress
        assertEquals("127.0.0.1", inet.hostname)
        assertEquals(8080, inet.port)
    }

    @Test
    fun `inetSocketAddress is cached`() {
        val addr = Address("127.0.0.1", 8080)
        val first = addr.inetSocketAddress
        val second = addr.inetSocketAddress
        assertSame(first, second)
    }
}
