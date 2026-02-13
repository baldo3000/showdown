package me.baldo3000.showdown.screen

import com.badlogic.gdx.Gdx
import ktx.actors.minusAssign
import ktx.actors.plusAssign
import ktx.ashley.getSystem
import ktx.log.logger
import me.baldo3000.showdown.Showdown
import me.baldo3000.showdown.ecs.system.ClientSystem
import me.baldo3000.showdown.ecs.system.HostSystem
import me.baldo3000.showdown.ui.HomeUI

class HomeScreen(game: Showdown) : ShowdownScreen(game) {
    private val ui = HomeUI(
        onHost = {
            game.setScreen<GameScreen>()
            engine.run { getSystem<HostSystem>().setProcessing(true) }
        },
        onJoin = {
            game.setScreen<GameScreen>()
            engine.run { getSystem<ClientSystem>().setProcessing(true) }
        },
        onCredits = { log.debug { "Credits button clicked" } },
        onQuit = { Gdx.app.exit() }
    )

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

    companion object {
        private val log = logger<HomeScreen>()
    }
}
