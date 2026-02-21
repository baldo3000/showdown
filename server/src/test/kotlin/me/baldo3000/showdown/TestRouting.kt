package me.baldo3000.showdown

import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.server.testing.*
import kotlinx.serialization.json.Json
import me.baldo3000.showdown.dto.LobbiesDTO
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertDoesNotThrow

class TestRouting {

    @Test
    fun `test lobbies route`() = testApplication {
        application {
            module()
        }
        val response = client.get("/lobbies")

        assertEquals(HttpStatusCode.OK, response.status)
        assertEquals(ContentType.Application.Json, response.contentType())
        assertDoesNotThrow {
            Json.decodeFromString<LobbiesDTO>(response.bodyAsText())
        }
    }
}
