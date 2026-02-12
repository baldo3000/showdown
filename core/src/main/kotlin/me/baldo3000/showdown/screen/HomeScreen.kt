package me.baldo3000.showdown.screen

import ktx.actors.minusAssign
import ktx.actors.onClick
import ktx.actors.plusAssign
import ktx.ashley.getSystem
import ktx.log.logger
import me.baldo3000.showdown.Showdown
import me.baldo3000.showdown.ecs.system.ClientNetworkSystem
import me.baldo3000.showdown.ecs.system.HostNetworkSystem
import me.baldo3000.showdown.ui.HomeUI

class HomeScreen(game: Showdown) : ShowdownScreen(game) {
    private val ui = HomeUI().apply {
        hostGameButton.onClick {
            game.setScreen<GameScreen>()
            engine.run { getSystem<HostNetworkSystem>().setProcessing(true) }
        }

        clientGameButton.onClick {
            game.setScreen<GameScreen>()
            engine.run { getSystem<ClientNetworkSystem>().setProcessing(true) }
        }

        creditsButton.onClick {
            log.debug { "Credits button clicked" }
        }
        quitGameButton.onClick {
            log.debug { "Quit button clicked" }
        }
    }

    override fun show() {
        super.show()
        log.debug { "HomeScreen is shown" }
        setupUI()
    }

    override fun hide() {
        super.hide()
        log.debug { "HomeScreen is hidden" }
        teardownUI()
    }

    private fun setupUI() {
        ui.run {
            stage += this.table
        }
    }

    private fun teardownUI() {
        ui.run {
            stage -= this.table
        }
    }

    override fun render(delta: Float) {
        engine.update(delta)
        stage.run {
            viewport.apply()
            act(delta)
            draw()
        }
    }

    companion object {
        private val log = logger<HomeScreen>()
    }
}
