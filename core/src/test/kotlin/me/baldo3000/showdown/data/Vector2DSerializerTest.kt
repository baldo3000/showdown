package me.baldo3000.showdown.data

import kotlinx.serialization.json.Json
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class Vector2DSerializerTest {

    private val json = Json

    @Test
    fun `serialize and deserialize Vector2D`() {
        val original = Vector2D(3.5f, -7.2f)
        val encoded = json.encodeToString(Vector2DSerializer, original)
        val decoded = json.decodeFromString(Vector2DSerializer, encoded)
        assertEquals(original.x, decoded.x)
        assertEquals(original.y, decoded.y)
    }

    @Test
    fun `deserialize from json`() {
        val jsonStr = """{"x":42.0,"y":99.0}"""
        val decoded = json.decodeFromString(Vector2DSerializer, jsonStr)
        assertEquals(42f, decoded.x, 0.0001f)
        assertEquals(99f, decoded.y, 0.0001f)
    }
}

