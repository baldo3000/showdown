package me.baldo3000.showdown.network

import kotlinx.serialization.json.Json
import me.baldo3000.showdown.data.Vector2D
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import kotlin.uuid.Uuid

@OptIn(kotlin.uuid.ExperimentalUuidApi::class)
class SnapshotsTest {

    private val json = Json

    @Test
    fun `PlayerSnapshot serialization round trip`() {
        val original = PlayerSnapshot(Uuid.random(), 100f, Vector2D(5f, 6f), Vector2D(7f, 8f))
        val encoded = json.encodeToString(PlayerSnapshot.serializer(), original)
        val decoded = json.decodeFromString(PlayerSnapshot.serializer(), encoded)
        assertEquals(original, decoded)
    }

    @Test
    fun `BulletSnapshot serialization round trip`() {
        val original = BulletSnapshot(Uuid.random(), Uuid.random(), 5f, Vector2D(10f, 20f), Vector2D(1f, 0f))
        val encoded = json.encodeToString(BulletSnapshot.serializer(), original)
        val decoded = json.decodeFromString(BulletSnapshot.serializer(), encoded)
        assertEquals(original, decoded)
    }

    @Test
    fun `WallSnapshot serialization round trip`() {
        val original = WallSnapshot(Uuid.random(), Vector2D(3f, 4f), Vector2D(10f, 5f))
        val encoded = json.encodeToString(WallSnapshot.serializer(), original)
        val decoded = json.decodeFromString(WallSnapshot.serializer(), encoded)
        assertEquals(original, decoded)
    }

    @Test
    fun `WorldSnapshot serialization round trip`() {
        val player = PlayerSnapshot(Uuid.random(), 80f, Vector2D(5f, 5f), Vector2D(1f, 1f))
        val bullet = BulletSnapshot(Uuid.random(), null, 10f, Vector2D(2f, 3f), Vector2D(-1f, 0f))
        val wall = WallSnapshot(Uuid.random(), Vector2D(0f, 4.5f), Vector2D(0.5f, 9f))
        val original = WorldSnapshot(listOf(player), listOf(bullet), listOf(wall), Uuid.random(), 7)

        val encoded = json.encodeToString(WorldSnapshot.serializer(), original)
        val decoded = json.decodeFromString(WorldSnapshot.serializer(), encoded)

        assertEquals(original.players.size, decoded.players.size)
        assertEquals(original.bullets.size, decoded.bullets.size)
        assertEquals(original.walls.size, decoded.walls.size)
        assertEquals(original.sessionId, decoded.sessionId)
        assertEquals(original.sequenceNumber, decoded.sequenceNumber)
        assertEquals(original.players[0].id, decoded.players[0].id)
        assertEquals(original.bullets[0].id, decoded.bullets[0].id)
    }
}
