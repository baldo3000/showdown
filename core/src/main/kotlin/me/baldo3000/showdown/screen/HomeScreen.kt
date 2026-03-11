package me.baldo3000.showdown.screen

import com.badlogic.gdx.Gdx
import ktx.actors.minusAssign
import ktx.actors.plusAssign
import ktx.log.logger
import me.baldo3000.showdown.Showdown
import me.baldo3000.showdown.network.LobbyDiscoveryClient
import me.baldo3000.showdown.network.NetworkConfig
import me.baldo3000.showdown.network.api.Address
import me.baldo3000.showdown.ui.HomeUI

class HomeScreen(game: Showdown) : ShowdownScreen(game) {
    private val discoveryClient = LobbyDiscoveryClient()
    private var ui: HomeUI

    init {
        ui = HomeUI(
            onHost = { updDropRate ->
                game.networkConfig.udpDropRate = updDropRate
                game.networkConfig.mode = NetworkConfig.Mode.HOST
                game.setScreen<GameScreen>()
            },
            onJoin = { ip, port, updDropRate ->
                game.networkConfig.udpDropRate = updDropRate
                game.networkConfig.mode = NetworkConfig.Mode.CLIENT
                game.networkConfig.hostAddress = Address(ip, port)
                game.setScreen<GameScreen>()
            },
            onSearchForServer = {
                ui.updateServerAddress("Searching...")
                discoveryClient.search(
                    onResult = { address ->
                        Gdx.app.postRunnable { ui.updateServerAddress("${address.ip}:${address.port}") }
                    },
                    onTimeout = {
                        Gdx.app.postRunnable { ui.updateServerAddress("No server found") }
                    }
                )
            },
            onCredits = { log.debug { "Credits button clicked" } },
            onQuit = { Gdx.app.exit() }
        )
    }

    override fun show() {
        super.show()
        log.debug { "HomeScreen is shown" }
        stage += ui.table
        stage += ui.udpDropRateTable
    }

    override fun hide() {
        super.hide()
        log.debug { "HomeScreen is hidden" }
        stage -= ui.table
        stage -= ui.udpDropRateTable
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
        discoveryClient.dispose()
    }

    companion object {
        private val log = logger<HomeScreen>()
    }
}
