package me.baldo3000.showdown.lobbies

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.plugins.*
import io.ktor.server.request.*
import io.ktor.server.request.ContentTransformationException
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.serialization.SerializationException
import me.baldo3000.showdown.dto.LobbiesDTO
import me.baldo3000.showdown.dto.LobbyDTO

fun Application.configureRouting() {
    var lobbies = LobbiesDTO(
        listOf(
            LobbyDTO("192.168.1.180", 1234),
            LobbyDTO("10.0.0.2", 5678),
            LobbyDTO("172.16.44.200", 9012)
        )
    )

    routing {
        get("/lobbies") {
            call.respond(message = lobbies)
        }

        post("/lobby") {
            try {
                val lobby = call.receive<LobbyDTO>()
                println("Adding lobby: $lobby")
                //lobbies = lobbies.copy(lobbies = lobbies.lobbies + lobby)
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
                val lobby = call.receive<LobbyDTO>()
                println("Removing lobby: $lobby")
                lobbies = lobbies.copy(lobbies = lobbies.lobbies - lobby)
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

private suspend fun RoutingCall.validLobby() {
    this.respond(HttpStatusCode.NoContent)
}

private suspend fun RoutingCall.invalidLobby() {
    this.respond(HttpStatusCode.BadRequest, "Invalid lobby data")
}

