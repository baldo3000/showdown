package me.baldo3000.showdown

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.plugins.statuspages.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import me.baldo3000.showdown.dto.LobbiesDTO
import me.baldo3000.showdown.dto.LobbyDTO

fun Application.configureRouting() {
    val lobbies = LobbiesDTO(
        listOf(
            LobbyDTO("192.168.1.180", 1234),
            LobbyDTO("10.0.0.2", 5678),
            LobbyDTO("172.16.44.200", 9012)
        )
    )

    install(StatusPages) {
        /*exception<IllegalStateException> { call, cause ->
            call.respondText("App in illegal state as ${cause.message}.")
        }*/
    }

    routing {
        get("/lobbies") {
            call.respond(message = lobbies)
        }

        get("/error-route") {
            throw IllegalStateException("This route always throws an exception")
        }

        post("/lobby") {
            try {
                val lobby = call.receive<LobbyDTO>()
                println("Received lobby: $lobby")
                call.respond(HttpStatusCode.NoContent)
            } catch (_: ContentTransformationException) {
                call.respond(HttpStatusCode.BadRequest, "Invalid lobby data")
            }
        }

        delete("/lobby") {
            val text = call.receiveText()
            call.respondText(
                text = text
            )
        }
    }
}

