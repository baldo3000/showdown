package me.baldo3000.showdown.screen

import com.badlogic.gdx.*
import ktx.actors.minusAssign
import ktx.actors.plusAssign
import ktx.ashley.getSystem
import ktx.log.logger
import me.baldo3000.showdown.Showdown
import me.baldo3000.showdown.ecs.processingInput
import me.baldo3000.showdown.ecs.reset
import me.baldo3000.showdown.ecs.system.*
import me.baldo3000.showdown.input.addInputProcessor
import me.baldo3000.showdown.network.NetworkConfig
import me.baldo3000.showdown.ui.GameEndUI
import me.baldo3000.showdown.ui.HostControlUI
import me.baldo3000.showdown.ui.PauseMenuUI
import kotlin.math.min

private const val MAX_DELTA_TIME = 1 / 20f

class GameScreen(game: Showdown) : ShowdownScreen(game) {

    private val menuUI = PauseMenuUI(
        onResume = { closeMenu() },
        onExit = { returnToMainMenu() }
    )
    private val gameEndUI = GameEndUI(
        onClose = { returnToMainMenu() }
    )
    private val hostControlUi = HostControlUI(
        onStartGame = { startGame() }
    )

    private val escProcessor = object : InputAdapter() {
        override fun keyDown(keycode: Int): Boolean {
            if (keycode == Input.Keys.ESCAPE) {
                if (menuVisible) closeMenu() else openMenu()
                return true
            }
            return false
        }
    }
    private var menuVisible = false
    private var endVisible = false
    private var hostControlEnabled = false
    private var savedInputProcessor: InputProcessor? = null

    init {
        engine.run {
            getSystem<GameEventsSystem>().apply {
                onGameEnd = ::openEnd
                onDisconnect = ::openDisconnect
            }
        }
    }

    override fun show() {
        super.show()
        log.debug { "GameScreen is shown" }
        // make sure ESC is always handled
        addInputProcessor(escProcessor)
        enableGameSystems()
    }

    override fun hide() {
        super.hide()
        closeMenu()
        closeEnd()
        disableGameSystems()
    }

    override fun render(delta: Float) {
        engine.update(min(delta, MAX_DELTA_TIME))

        if (menuVisible || endVisible || hostControlEnabled) {
            stage.viewport.apply()
            stage.run {
                act(min(delta, MAX_DELTA_TIME))
                draw()
            }
        }
    }

    override fun dispose() {
        log.debug { "GameScreen has been disposed" }
        super.dispose()
    }

    private fun openMenu() {
        if (menuVisible) return
        menuVisible = true
        stage += menuUI.table
        savedInputProcessor = Gdx.input.inputProcessor
        Gdx.input.inputProcessor = InputMultiplexer(menuUI.table.stage, escProcessor)
    }

    private fun closeMenu() {
        if (!menuVisible) return
        menuVisible = false
        stage -= menuUI.table
        Gdx.input.inputProcessor = savedInputProcessor
    }

    private fun openEnd(placement: Int) {
        closeMenu()
        if (endVisible) return
        endVisible = true
        gameEndUI.setPlacement(placement)
        stage += gameEndUI.table
        savedInputProcessor = Gdx.input.inputProcessor
        Gdx.input.inputProcessor = gameEndUI.table.stage
        engine.getSystem<RenderSystem>().setProcessing(false)
    }

    private fun openDisconnect() {
        closeMenu()
        if (endVisible) return
        endVisible = true
        gameEndUI.setDisconnected()
        stage += gameEndUI.table
        savedInputProcessor = Gdx.input.inputProcessor
        Gdx.input.inputProcessor = gameEndUI.table.stage
        engine.getSystem<RenderSystem>().setProcessing(false)
    }

    private fun closeEnd() {
        if (!endVisible) return
        endVisible = false
        stage -= gameEndUI.table
        Gdx.input.inputProcessor = savedInputProcessor
    }

    private fun startGame() {
        stage -= hostControlUi.table
        hostControlEnabled = false
        engine.processingInput = true
    }

    private fun returnToMainMenu() {
        game.setScreen<HomeScreen>()
        engine.processingInput = false
    }

    private fun enableGameSystems() {
        engine.reset()
        engine.run {
            when (game.networkConfig.mode) {
                NetworkConfig.Mode.HOST -> {
                    stage += hostControlUi.table
                    hostControlEnabled = true
                    processingInput = false
                    getSystem<HostSystem>().setProcessing(true)
                }

                NetworkConfig.Mode.CLIENT -> {
                    processingInput = true
                    getSystem<ClientSystem>().setProcessing(true)
                }
            }
            getSystem<CameraSystem>().setProcessing(true)
            getSystem<CollisionSystem>().setProcessing(true)
            getSystem<GameEventsSystem>().setProcessing(true)
            getSystem<MoveSystem>().setProcessing(true)
            getSystem<RemoveSystem>().setProcessing(true)
            getSystem<RenderSystem>().setProcessing(true)
        }
    }

    private fun disableGameSystems() {
        engine.reset()
        engine.run {
            processingInput = false
            getSystem<CameraSystem>().setProcessing(false)
            getSystem<CollisionSystem>().setProcessing(false)
            getSystem<GameEventsSystem>().setProcessing(false)
            getSystem<MoveSystem>().setProcessing(false)
            getSystem<RemoveSystem>().setProcessing(false)
            getSystem<RenderSystem>().setProcessing(false)

            getSystem<HostSystem>().setProcessing(false)
            getSystem<ClientSystem>().setProcessing(false)
        }
    }

    companion object {
        private val log = logger<GameScreen>()
    }
}
