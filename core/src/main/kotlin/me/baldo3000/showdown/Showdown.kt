package me.baldo3000.showdown

import com.badlogic.ashley.core.Engine
import com.badlogic.ashley.core.PooledEngine
import com.badlogic.gdx.Application
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.InputMultiplexer
import com.badlogic.gdx.graphics.g2d.Batch
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.utils.viewport.ExtendViewport
import ktx.app.KtxGame
import ktx.log.logger
import me.baldo3000.showdown.ecs.component.*
import me.baldo3000.showdown.ecs.system.*
import me.baldo3000.showdown.event.GameEventHandler
import me.baldo3000.showdown.screen.GameScreen
import me.baldo3000.showdown.screen.HomeScreen
import me.baldo3000.showdown.screen.ShowdownScreen

const val UNIT_SCALE = 1 / 16f

/** [com.badlogic.gdx.ApplicationListener] implementation shared by all platforms. */
class Showdown : KtxGame<ShowdownScreen>() {
    val gameViewport = ExtendViewport(16f, 9f)
    val batch: Batch by lazy { SpriteBatch() }
    val eventHandler by lazy { GameEventHandler() }
    val engine: Engine by lazy {
        PooledEngine(10, 1000, 10, 1000).apply {
            addSystem(PlayerInputSystem(gameViewport))
            addSystem(EventSystem(eventHandler))
            addSystem(MoveSystem(eventHandler))
            addSystem(CollisionSystem())
            addSystem(CameraSystem(gameViewport))
            addSystem(RenderSystem(batch, gameViewport))
            addSystem(RemoveSystem())
            addSystem(HostNetworkSystem(8080, eventHandler))
            //addSystem(ClientNetworkSystem(gameViewport, eventHandler))
        }
    }

    override fun create() {
        loadComponentMappers()
        Gdx.app.logLevel = Application.LOG_DEBUG
        Gdx.input.inputProcessor = InputMultiplexer()
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

    private fun loadComponentMappers() {
        CameraComponent.mapper
        ColliderComponent.mapper
        DamageComponent.mapper
        GraphicComponent.mapper
        HealthComponent.mapper
        IdComponent.mapper
        InputComponent.mapper
        MoveComponent.mapper
        RemoveComponent.mapper
        TransformComponent.mapper
    }

    companion object {
        private val log = logger<Showdown>()
    }
}
