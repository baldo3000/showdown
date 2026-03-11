package me.baldo3000.showdown.network

import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.coroutines.*
import kotlinx.serialization.json.Json
import ktx.log.logger
import me.baldo3000.showdown.dto.AddressDTO
import me.baldo3000.showdown.dto.LobbiesDTO
import me.baldo3000.showdown.network.api.Address

private const val GET_TIMEOUT_MS = 3000L

class LobbyHttpClient {
    private val client = HttpClient(CIO) {
        install(ContentNegotiation) {
            json()
        }
    }
    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private var job: Job? = null

    fun fetchLobbies(
        lobbyServer: Address,
        onResult: (LobbiesDTO) -> Unit = {},
        onTimeout: () -> Unit = {},
    ) {
        job?.cancel()
        job = scope.launch {
            try {
                val lobbies = withTimeoutOrNull(GET_TIMEOUT_MS) {
                    val url = "http://${lobbyServer.ip}:${lobbyServer.port}$LOBBIES_ROUTE"
                    val response = client.get(url)
                    log.debug { "GET $url → ${response.status}" }
                    Json.decodeFromString<LobbiesDTO>(response.bodyAsText())
                }
                if (lobbies != null) {
                    onResult(lobbies)
                } else {
                    log.debug { "GET lobbies timed out after ${GET_TIMEOUT_MS}ms" }
                    onTimeout()
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                log.error { "GET lobbies error: ${e.message}" }
                onTimeout()
            }
        }
    }

    fun updateLobby(
        lobbyServer: Address,
        hostAddress: Address,
        delete: Boolean = false
    ) {
        job?.cancel()
        job = scope.launch {
            try {
                withTimeout(GET_TIMEOUT_MS) {
                    val url = "http://${lobbyServer.ip}:${lobbyServer.port}$LOBBY_ROUTE"
                    if (!delete) {
                        client.post(url) {
                            contentType(ContentType.Application.Json)
                            setBody(AddressDTO(hostAddress.ip, hostAddress.port))
                        }
                    } else {
                        client.delete(url) {
                            contentType(ContentType.Application.Json)
                            setBody(AddressDTO(hostAddress.ip, hostAddress.port))
                        }
                    }
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                log.error { "POST lobby error: ${e.message}" }
            }
        }
    }

    fun dispose() {
        scope.cancel()
        client.close()
    }

    companion object {
        private val log = logger<LobbyHttpClient>()
    }
}
