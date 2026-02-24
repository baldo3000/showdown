package me.baldo3000.showdown.lobbies

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.plugins.*
import io.ktor.server.request.*
import io.ktor.server.request.ContentTransformationException
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.util.collections.*
import kotlinx.serialization.SerializationException
import me.baldo3000.showdown.dto.LobbiesDTO
import me.baldo3000.showdown.dto.LobbyDTO

const val LOBBY_EXPIRE_TIME_MS = 60 * 1000L // 1 minute

fun Application.configureRouting() {
    val lobbies = ConcurrentSet<Lobby>()

    // TODO: Remove
    lobbies.add(Lobby("192.168.1.180", 1234))
    lobbies.add(Lobby("192.168.1.181", 567))
    lobbies.add(Lobby("192.168.1.182", 89))

    routing {
        get("/lobbies") {
            removeExpiredLobbies(lobbies)
            call.respond(LobbiesDTO(lobbies.map { LobbyDTO(it.ip, it.port) }))
        }

        post("/lobby") {
            try {
                val lobbyDTO = call.receive<LobbyDTO>()
                println("Adding lobby: $lobbyDTO")
                val lobby = Lobby(lobbyDTO.ip, lobbyDTO.port)
                lobbies.add(lobby)
                call.validLobby()
            } catch (_: ContentTransformationException) {
                call.invalidLobby()
            } catch (_: SerializationException) {
                call.invalidLobby()
            } catch (_: BadRequestException) {
                call.invalidLobby()
            }
        }

        delete("/lobby") {
            try {
                val lobbyDTO = call.receive<LobbyDTO>()
                println("Removing lobby: $lobbyDTO")
                lobbies.remove(Lobby(lobbyDTO.ip, lobbyDTO.port))
                call.validLobby()
            } catch (_: ContentTransformationException) {
                call.invalidLobby()
            } catch (_: SerializationException) {
                call.invalidLobby()
            } catch (_: BadRequestException) {
                call.invalidLobby()
            }
        }
    }
}

private fun removeExpiredLobbies(lobbies: MutableSet<Lobby>) {
    val now = System.currentTimeMillis()
    lobbies.removeIf { lobby -> now - lobby.dateAdded > LOBBY_EXPIRE_TIME_MS }
}

private suspend fun RoutingCall.validLobby() {
    this.respond(HttpStatusCode.NoContent)
}

private suspend fun RoutingCall.invalidLobby() {
    this.respond(HttpStatusCode.BadRequest, "Invalid lobby data")
}
