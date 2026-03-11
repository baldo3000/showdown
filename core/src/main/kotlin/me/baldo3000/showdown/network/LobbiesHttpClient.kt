package me.baldo3000.showdown.network

import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import kotlinx.coroutines.*
import kotlinx.serialization.json.Json
import ktx.log.logger
import me.baldo3000.showdown.dto.LobbiesDTO
import me.baldo3000.showdown.network.api.Address

private const val GET_TIMEOUT_MS = 3000L

class LobbiesHttpClient {
    private val client = HttpClient(CIO)
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

    fun dispose() {
        scope.cancel()
        client.close()
    }

    companion object {
        private val log = logger<LobbiesHttpClient>()
    }
}
