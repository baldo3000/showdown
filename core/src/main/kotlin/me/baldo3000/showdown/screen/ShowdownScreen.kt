package me.baldo3000.showdown.screen

import com.badlogic.ashley.core.Engine
import com.badlogic.gdx.utils.viewport.Viewport
import ktx.app.KtxScreen
import me.baldo3000.showdown.Showdown

abstract class ShowdownScreen(val game: Showdown) : KtxScreen {
    val gameViewport: Viewport = game.gameViewport
    val engine: Engine = game.engine

    override fun resize(width: Int, height: Int) {
        gameViewport.update(width, height, true)
    }
}
