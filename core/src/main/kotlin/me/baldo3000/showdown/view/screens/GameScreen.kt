package me.baldo3000.showdown.view.screens

import ktx.ashley.entity
import ktx.log.logger
import me.baldo3000.showdown.Showdown
import kotlin.math.min

private const val MAX_DELTA_TIME = 1 / 20f

class GameScreen(game: Showdown) : ShowdownScreen(game) {

    override fun show() {
        log.debug { "GameScreen is shown" }

        engine.entity {

        }
    }

    override fun render(delta: Float) {
        engine.update(min(delta, MAX_DELTA_TIME))
    }

    companion object {
        private val log = logger<GameScreen>()
    }
}
