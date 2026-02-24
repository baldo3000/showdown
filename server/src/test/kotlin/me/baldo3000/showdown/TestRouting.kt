package me.baldo3000.showdown

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.testing.*
import me.baldo3000.showdown.dto.LobbiesDTO
import me.baldo3000.showdown.dto.LobbyDTO
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertDoesNotThrow

class TestRouting {
    private fun setUp(body: suspend (HttpClient) -> Unit) = testApplication {
        application { module() }
        client = createClient { install(ContentNegotiation) { json() } }
        body(client)
    }

    @Test
    fun `getting at lobbies should return a LobbiesDTO`() = setUp { client ->
        val response = client.httpMethod(HttpMethod.Get, LOBBIES_URL)
        assertEquals(HttpStatusCode.OK, response.status)
        assertEquals(ContentType.Application.Json.withCharset(Charsets.UTF_8), response.contentType())
        assertDoesNotThrow {
            response.body<LobbiesDTO>()
        }
    }

    @Test
    fun `posting a LobbyDTO at lobby should be accepted`() = setUp { client ->
        val response = client.httpMethod(HttpMethod.Post, LOBBY_URL, exampleLobby)
        assertEquals(HttpStatusCode.NoContent, response.status)
    }

    @Test
    fun `posting an object different than LobbyDTO at lobby should not be accepted`() = setUp { client ->
        val response = client.httpMethod(HttpMethod.Post, LOBBY_URL, exampleNotLobby)
        assertEquals(HttpStatusCode.BadRequest, response.status)
    }

    @Test
    fun `deleting a LobbyDTO at lobby should be accepted`() = setUp { client ->
        val response = client.httpMethod(HttpMethod.Delete, LOBBY_URL, exampleLobby)
        assertEquals(HttpStatusCode.NoContent, response.status)
    }

    @Test
    fun `deleting an object different than LobbyDTO at lobby should not be accepted`() = setUp { client ->
        val response = client.httpMethod(HttpMethod.Delete, LOBBY_URL, exampleNotLobby)
        assertEquals(HttpStatusCode.BadRequest, response.status)
    }

    private suspend fun HttpClient.httpMethod(method: HttpMethod, url: String, body: Any? = null): HttpResponse {
        return this.request(url) {
            this.method = method
            contentType(ContentType.Application.Json)
            setBody(body)
        }
    }

    companion object {
        private const val LOBBIES_URL = "/lobbies"
        private const val LOBBY_URL = "/lobby"
        private val exampleLobby = LobbyDTO("127.0.0.1", 8080)
        private val exampleNotLobby = listOf(1, 2, 3)
    }
}
