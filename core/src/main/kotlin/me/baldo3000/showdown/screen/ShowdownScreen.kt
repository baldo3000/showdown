package me.baldo3000.showdown.screen

import com.badlogic.ashley.core.Engine
import com.badlogic.gdx.graphics.g2d.Batch
import com.badlogic.gdx.utils.viewport.Viewport
import ktx.app.KtxScreen
import me.baldo3000.showdown.Showdown

abstract class ShowdownScreen(
    val game: Showdown,
    val batch: Batch = game.batch,
    val gameViewport: Viewport = game.gameViewport,
    val engine: Engine = game.engine
) : KtxScreen {
    override fun resize(width: Int, height: Int) {
        gameViewport.update(width, height, true)
    }
}
