package me.baldo3000.showdown.screen

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Input
import ktx.log.logger
import me.baldo3000.showdown.Showdown

class HomeScreen(game: Showdown) : ShowdownScreen(game) {
    override fun show() {
        log.debug { "HomeScreen is shown" }
    }

    override fun render(delta: Float) {
        if (Gdx.input.isKeyJustPressed(Input.Keys.A)) {
            game.setScreen<GameScreen>()
        }
    }

    companion object {
        private val log = logger<HomeScreen>()
    }
}
