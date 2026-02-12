package me.baldo3000.showdown.screen

import com.badlogic.ashley.core.Engine
import com.badlogic.gdx.scenes.scene2d.Stage
import com.badlogic.gdx.utils.viewport.Viewport
import ktx.app.KtxScreen
import me.baldo3000.showdown.Showdown

abstract class ShowdownScreen(game: Showdown) : KtxScreen {
    val gameViewport: Viewport = game.gameViewport
    val engine: Engine = game.engine
    val stage: Stage = game.stage

    override fun resize(width: Int, height: Int) {
        gameViewport.update(width, height, true)
        stage.viewport.update(width, height, true)
    }
}
