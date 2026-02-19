package me.baldo3000.showdown.data

import kotlinx.serialization.json.Json
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class Vector3DSerializerTest {

    private val json = Json

    @Test
    fun `serialize and deserialize Vector3D`() {
        val original = Vector3D(1.5f, -2.3f, 4.7f)
        val encoded = json.encodeToString(Vector3DSerializer, original)
        val decoded = json.decodeFromString(Vector3DSerializer, encoded)
        assertEquals(original.x, decoded.x)
        assertEquals(original.y, decoded.y)
        assertEquals(original.z, decoded.z)
    }

    @Test
    fun `deserialize from json`() {
        val jsonStr = """{"x":10.0,"y":20.0,"z":30.0}"""
        val decoded = json.decodeFromString(Vector3DSerializer, jsonStr)
        assertEquals(10f, decoded.x, 0.0001f)
        assertEquals(20f, decoded.y, 0.0001f)
        assertEquals(30f, decoded.z, 0.0001f)
    }
}

