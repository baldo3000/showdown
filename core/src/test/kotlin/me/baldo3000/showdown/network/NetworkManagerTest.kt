package me.baldo3000.showdown.network

import com.badlogic.gdx.ApplicationAdapter
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.backends.headless.HeadlessApplication
import com.badlogic.gdx.backends.headless.HeadlessApplicationConfiguration
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import kotlin.uuid.Uuid

class NetworkManagerTest {

    private lateinit var host: HostNetworkManager
    private lateinit var client: ClientNetworkManager

    companion object {
        @JvmStatic
        @BeforeAll
        fun setUpGdx() {
            // Headless LibGDX app
            val config = HeadlessApplicationConfiguration()
            HeadlessApplication(object : ApplicationAdapter() {}, config)
        }
    }

    @BeforeEach
    fun setUp() {
        host = HostNetworkManager()
        client = ClientNetworkManager()
    }

    @AfterEach
    fun tearDown() {
        client.stop()
        host.stop()
    }

    @Test
    fun `client receives uuid on connect`() = runBlocking {
        host.start()
        waitUntilHostReady()

        var receivedId: Uuid? = null
        client = ClientNetworkManager(onConnect = { Gdx.app.postRunnable { receivedId = it } })
        client.connect("127.0.0.1", host.port!!)

        withTimeout(2000) {
            while (receivedId == null) delay(10)
        }

        assertNotNull(receivedId)
        assertEquals(receivedId, client.id)
    }

    @Test
    fun `host saves peer id when client connects`() = runBlocking {
        var connectedPeerId: Uuid? = null
        host = HostNetworkManager(onPeerConnect = { Gdx.app.postRunnable { connectedPeerId = it } })
        host.start()
        waitUntilHostReady()

        client.connect("127.0.0.1", host.port!!)

        withTimeout(2000) {
            while (connectedPeerId == null) delay(10)
        }

        assertNotNull(connectedPeerId)
        assertTrue(host.connectedPeerIds.contains(connectedPeerId))
    }

    @Test
    fun `host receives udp message from client`() = runBlocking {
        host.start()
        waitUntilHostReady()

        client = ClientNetworkManager(onConnect = {
            Gdx.app.postRunnable { client.sendToHost("hello".toByteArray()) }
        })
        client.connect("127.0.0.1", host.port!!)

        var received: ByteArray? = null
        withTimeout(2000) {
            while (received == null) {
                received = host.receiveChannel.tryReceive().getOrNull()
                delay(10)
            }
        }

        assertEquals("hello", received!!.decodeToString())
    }

    @Test
    fun `client receives udp message from host`() = runBlocking {
        var clientConnected = false
        host = HostNetworkManager(onPeerConnect = {
            Gdx.app.postRunnable { clientConnected = true }
        })
        host.start()
        waitUntilHostReady()

        client.connect("127.0.0.1", host.port!!)

        withTimeout(2000) {
            while (!clientConnected) delay(10)
        }
        delay(100)
        host.sendToClients("world".toByteArray())

        var received: ByteArray? = null
        withTimeout(2000) {
            while (received == null) {
                received = client.receiveChannel.tryReceive().getOrNull()
                delay(10)
            }
        }

        assertEquals("world", received!!.decodeToString())
    }

    @Test
    fun `host executes disconnect callback when client stops`() = runBlocking {
        var disconnectedPeerId: Uuid? = null
        var connectedPeerId: Uuid? = null
        host = HostNetworkManager(
            onPeerConnect = { Gdx.app.postRunnable { connectedPeerId = it } },
            onPeerDisconnect = { Gdx.app.postRunnable { disconnectedPeerId = it } }
        )
        host.start()
        waitUntilHostReady()

        client.connect("127.0.0.1", host.port!!)
        withTimeout(2000) { while (connectedPeerId == null) delay(10) }
        assertTrue(host.connectedPeerIds.contains(connectedPeerId))

        client.stop()
        withTimeout(2000) { while (disconnectedPeerId == null) delay(10) }

        assertEquals(connectedPeerId, disconnectedPeerId)
        assertFalse(host.connectedPeerIds.contains(disconnectedPeerId))
    }

    @Test
    fun `client executes failure callback when host is not available`() = runBlocking {
        var failed = false
        client = ClientNetworkManager(onConnectionFailure = { Gdx.app.postRunnable { failed = true } })
        client.connect("127.0.0.1", 19999) // no host on this port

        withTimeout(5000) {
            while (!failed) delay(10)
        }

        assertTrue(failed)
    }

    @Test
    fun `multiple clients can connect to host`() = runBlocking {
        host.start()
        waitUntilHostReady()

        val client1 = ClientNetworkManager()
        val client2 = ClientNetworkManager()

        try {
            client1.connect("127.0.0.1", host.port!!)
            client2.connect("127.0.0.1", host.port!!)

            withTimeout(2000) {
                while (host.connectedPeerIds.size < 2) delay(10)
            }

            assertEquals(2, host.connectedPeerIds.size)
        } finally {
            client1.stop()
            client2.stop()
        }
    }

    private suspend fun waitUntilHostReady() {
        withTimeout(2000) {
            while (host.port == null) delay(10)
        }
    }
}
