package me.baldo3000.showdown

import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.server.testing.*
import kotlinx.serialization.json.Json
import me.baldo3000.showdown.dto.LobbiesDTO
import me.baldo3000.showdown.dto.LobbyDTO
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertDoesNotThrow

class TestRouting {

    private val json = Json

    @Test
    fun `getting at lobbies should return a LobbiesDTO`() = testApplication {
        application {
            module()
        }
        val response = client.get("/lobbies")

        assertEquals(HttpStatusCode.OK, response.status)
        assertEquals(ContentType.Application.Json.withCharset(Charsets.UTF_8), response.contentType())
        assertDoesNotThrow {
            json.decodeFromString<LobbiesDTO>(response.bodyAsText())
        }
    }

    @Test
    fun `posting a LobbyDTO at lobby should be accepted`() = testApplication {
        application {
            module()
        }

        val response = client.post("/lobby") {
            contentType(ContentType.Application.Json)
            setBody(json.encodeToString(LobbyDTO("127.0.0.1", 8080)))
        }

        assertEquals(HttpStatusCode.NoContent, response.status)
    }

    @Test
    fun `posting an object different than LobbyDTO at lobby should not be accepted`() = testApplication {
        application {
            module()
        }

        val response = client.post("/lobby") {
            contentType(ContentType.Application.Json)
            setBody(json.encodeToString(listOf(1, 2, 3)))
        }

        assertEquals(HttpStatusCode.BadRequest, response.status)
    }
}
