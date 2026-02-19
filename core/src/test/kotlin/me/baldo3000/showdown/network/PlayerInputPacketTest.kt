package me.baldo3000.showdown.network

import kotlinx.serialization.json.Json
import me.baldo3000.showdown.data.Vector2D
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test
import kotlin.uuid.Uuid

@OptIn(kotlin.uuid.ExperimentalUuidApi::class)
class PlayerInputPacketTest {

    private val json = Json

    @Test
    fun `default touching is null`() {
        val packet = PlayerInputPacket(Uuid.random(), 0, 0)
        assertNull(packet.touching)
    }

    @Test
    fun `default sequenceNumber is 0`() {
        val packet = PlayerInputPacket(Uuid.random(), 0, 0)
        assertEquals(0, packet.sequenceNumber)
    }

    @Test
    fun `serialize and deserialize round trip`() {
        val original = PlayerInputPacket(Uuid.random(), 1, -1, Vector2D(10f, 20f), 5)
        val encoded = json.encodeToString(PlayerInputPacket.serializer(), original)
        val decoded = json.decodeFromString(PlayerInputPacket.serializer(), encoded)
        assertEquals(original, decoded)
    }
}
