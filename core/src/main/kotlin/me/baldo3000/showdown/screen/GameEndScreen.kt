package me.baldo3000.showdown.screen

import ktx.actors.minusAssign
import ktx.actors.plusAssign
import me.baldo3000.showdown.Showdown
import me.baldo3000.showdown.ui.GameEndUI

class GameEndScreen(game: Showdown) : ShowdownScreen(game) {
    private val ui = GameEndUI(
        onClose = { game.setScreen<HomeScreen>() }
    )

    var placement: Int = 0

    override fun show() {
        super.show()
        ui.updatePlacement(placement)
        stage += ui.table
    }

    override fun hide() {
        super.hide()
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
}
