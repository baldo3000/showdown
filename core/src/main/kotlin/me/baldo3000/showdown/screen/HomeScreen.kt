package me.baldo3000.showdown.screen

import com.badlogic.gdx.Gdx
import ktx.actors.minusAssign
import ktx.actors.plusAssign
import ktx.log.logger
import me.baldo3000.showdown.Showdown
import me.baldo3000.showdown.network.LobbiesHttpClient
import me.baldo3000.showdown.network.LobbyDiscoveryClient
import me.baldo3000.showdown.network.NetworkConfig
import me.baldo3000.showdown.network.api.Address
import me.baldo3000.showdown.ui.HomeUI

class HomeScreen(game: Showdown) : ShowdownScreen(game) {
    private val discoveryClient = LobbyDiscoveryClient()
    private val lobbiesHttpClient = LobbiesHttpClient()
    private var ui: HomeUI

    init {
        ui = HomeUI(
            onHost = { updDropRate ->
                game.networkConfig.udpDropRate = updDropRate
                game.networkConfig.mode = NetworkConfig.Mode.HOST
                game.setScreen<GameScreen>()
            },
            onJoin = ::joinLobby,
            onSearchForServer = {
                ui.updateServerAddress("Searching...")
                discoveryClient.search(
                    onResult = { address ->
                        Gdx.app.postRunnable {
                            game.networkConfig.lobbyServerAddress = Address(address.ip, address.port)
                            ui.updateServerAddress("${address.ip}:${address.port}")
                        }
                    },
                    onTimeout = {
                        Gdx.app.postRunnable {
                            game.networkConfig.lobbyServerAddress = null
                            ui.updateServerAddress("No server found. (Needed for posting and searching lobbies)")
                        }
                    }
                )
            },
            onRefreshLobbies = {
                val serverAddress = game.networkConfig.lobbyServerAddress
                if (serverAddress == null) {
                    ui.setLobbiesStatus("Search for a server first")
                } else {
                    ui.setLobbiesStatus("Loading...")
                    lobbiesHttpClient.fetchLobbies(
                        lobbyServer = serverAddress,
                        onResult = { lobbiesDTO -> Gdx.app.postRunnable { ui.updateLobbies(lobbiesDTO.lobbies) } },
                        onTimeout = { Gdx.app.postRunnable { ui.setLobbiesStatus("Failed to reach server") } }
                    )
                }
            },
            onLobbySelected = ::joinLobby,
            onCredits = { log.debug { "Credits button clicked" } },
            onQuit = { Gdx.app.exit() }
        )
    }

    private fun joinLobby(ip: String, port: Int, udpDropRate: Float) {
        game.networkConfig.udpDropRate = udpDropRate
        game.networkConfig.mode = NetworkConfig.Mode.CLIENT
        game.networkConfig.hostAddress = Address(ip, port)
        game.setScreen<GameScreen>()
    }

    override fun show() {
        super.show()
        log.debug { "HomeScreen is shown" }
        stage += ui.table
    }

    override fun hide() {
        super.hide()
        log.debug { "HomeScreen is hidden" }
        stage -= ui.table
    }

    override fun render(delta: Float) {
        engine.update(delta)
        stage.run {
            viewport.apply()
            act(delta)
            draw()
        }
    }

    override fun dispose() {
        super.dispose()
        lobbiesHttpClient.dispose()
        discoveryClient.dispose()
    }

    companion object {
        private val log = logger<HomeScreen>()
    }
}
