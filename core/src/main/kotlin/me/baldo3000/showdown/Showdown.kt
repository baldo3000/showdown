package me.baldo3000.showdown

import com.badlogic.ashley.core.Engine
import com.badlogic.ashley.core.PooledEngine
import com.badlogic.gdx.Application
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.g2d.Batch
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.utils.viewport.FitViewport
import ktx.app.KtxGame
import ktx.log.logger
import me.baldo3000.showdown.ecs.system.MoveSystem
import me.baldo3000.showdown.ecs.system.RemoveSystem
import me.baldo3000.showdown.ecs.system.RenderSystem
import me.baldo3000.showdown.screen.GameScreen
import me.baldo3000.showdown.screen.HomeScreen
import me.baldo3000.showdown.screen.ShowdownScreen

const val UNIT_SCALE = 1 / 16f

/** [com.badlogic.gdx.ApplicationListener] implementation shared by all platforms. */
class Showdown : KtxGame<ShowdownScreen>() {
    val gameViewport = FitViewport(16f, 9f)
    val batch: Batch by lazy { SpriteBatch() }
    val engine: Engine by lazy {
        PooledEngine(10, 1000, 10, 1000).apply {
            addSystem(MoveSystem())
            addSystem(RenderSystem(batch, gameViewport))
            addSystem(RemoveSystem())
        }
    }

    override fun create() {
        Gdx.app.logLevel = Application.LOG_DEBUG
        log.debug { "Game instance created" }
        addScreen(HomeScreen(this))
        addScreen(GameScreen(this))
        setScreen<GameScreen>()
    }

    override fun dispose() {
        super.dispose()
        log.debug { "Max amount of sprites: ${(batch as SpriteBatch).maxSpritesInBatch}" }
        batch.dispose()
    }

    companion object {
        private val log = logger<Showdown>()
    }
}
