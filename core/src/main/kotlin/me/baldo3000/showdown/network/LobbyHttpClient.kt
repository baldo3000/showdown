package me.baldo3000.showdown.network

import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.coroutines.*
import ktx.log.logger
import me.baldo3000.showdown.dto.AddressDTO
import me.baldo3000.showdown.network.api.Address

private const val TIMEOUT_MS = 3000L

class LobbyHttpClient {
    private val client = HttpClient(CIO) {
        install(ContentNegotiation) {
            json()
        }
    }
    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private var job: Job? = null

    fun updateLobby(
        lobbyServer: Address,
        hostAddress: Address,
        delete: Boolean = false
    ) {
        job?.cancel()
        job = scope.launch {
            try {
                withTimeout(TIMEOUT_MS) {
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
                log.error { "Update lobby error: ${e.message}" }
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
