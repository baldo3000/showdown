package me.baldo3000.showdown

import com.badlogic.ashley.core.Engine
import com.badlogic.ashley.core.PooledEngine
import com.badlogic.gdx.Application
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.InputMultiplexer
import com.badlogic.gdx.scenes.scene2d.Stage
import com.badlogic.gdx.utils.viewport.ExtendViewport
import com.badlogic.gdx.utils.viewport.ScreenViewport
import ktx.app.KtxGame
import ktx.log.logger
import me.baldo3000.showdown.data.Vector2D
import me.baldo3000.showdown.data.Vector3D
import me.baldo3000.showdown.ecs.component.*
import me.baldo3000.showdown.ecs.component.event.PlayerDeathComponent
import me.baldo3000.showdown.ecs.system.*
import me.baldo3000.showdown.input.addInputProcessor
import me.baldo3000.showdown.network.BulletSnapshot
import me.baldo3000.showdown.network.PlayerInputPacket
import me.baldo3000.showdown.network.PlayerSnapshot
import me.baldo3000.showdown.network.WorldSnapshot
import me.baldo3000.showdown.screen.GameScreen
import me.baldo3000.showdown.screen.HomeScreen
import me.baldo3000.showdown.screen.ShowdownScreen
import me.baldo3000.showdown.ui.Textures
import me.baldo3000.showdown.ui.createSkin

const val V_WIDTH = 16
const val V_HEIGHT = 9
const val UNIT_SCALE = 1 / 16f

/** [com.badlogic.gdx.ApplicationListener] implementation shared by all platforms. */
class Showdown : KtxGame<ShowdownScreen>() {
    val gameViewport = ExtendViewport(V_WIDTH.toFloat(), V_HEIGHT.toFloat())
    val stage: Stage by lazy {
        val result = Stage(ScreenViewport())
        addInputProcessor(result)
        result
    }
    val engine: Engine by lazy {
        PooledEngine(10, 1000, 10, 1000).apply {
            addSystem(PlayerInputSystem(gameViewport).apply { setProcessing(false) })
            addSystem(MoveSystem().apply { setProcessing(false) })
            addSystem(CollisionSystem().apply { setProcessing(false) })
            addSystem(CameraSystem(gameViewport).apply { setProcessing(false) })
            addSystem(RenderSystem(stage.batch, gameViewport).apply { setProcessing(false) })
            addSystem(HostSystem().apply { setProcessing(false) })
            addSystem(ClientSystem(gameViewport).apply { setProcessing(false) })
            addSystem(GameEventsSystem().apply { setProcessing(false) })
            addSystem(RemoveSystem().apply { setProcessing(false) })
        }
    }

    override fun create() {
        load()
        Gdx.app.logLevel = Application.LOG_DEBUG
        Gdx.input.inputProcessor = InputMultiplexer()
        createSkin()
        log.debug { "Game instance created" }
        addScreen(HomeScreen(this))
        addScreen(GameScreen(this))
        setScreen<HomeScreen>()
    }

    override fun dispose() {
        super.dispose()
        Textures.dispose()
        stage.dispose()
    }

    private fun load() {
        loadComponentMappers()
        loadSerializers()
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
        PlayerDeathComponent.mapper
    }

    private fun loadSerializers() {
        PlayerInputPacket.serializer()
        PlayerSnapshot.serializer()
        BulletSnapshot.serializer()
        WorldSnapshot.serializer()
        Vector2D.serializer()
        Vector3D.serializer()
    }

    companion object {
        private val log = logger<Showdown>()
    }
}
