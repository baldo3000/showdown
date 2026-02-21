package me.baldo3000.showdown

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.plugins.statuspages.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.serialization.json.Json
import me.baldo3000.showdown.dto.LobbiesDTO
import me.baldo3000.showdown.dto.LobbyDTO

fun Application.configureRouting() {

    val json = Json

    val lobbies = LobbiesDTO(listOf(
        LobbyDTO("192.168.1.180", 1234),
        LobbyDTO("10.0.0.2", 5678),
        LobbyDTO("172.16.44.200", 9012)
    ))

    install(StatusPages) {
        exception<IllegalStateException> { call, cause ->
            call.respondText("App in illegal state as ${cause.message}.")
        }
    }

    routing {
        get("/lobbies") {
            call.respondText(
                text = json.encodeToString(lobbies),
                contentType = ContentType.Application.Json
            )
        }

        get("/illegal-state") {
            throw IllegalStateException("This route always throws an exception")
        }

        post("/lobbies") {
            val text = call.receiveText()
            call.respondText(
                text = text
            )
        }
    }
}

