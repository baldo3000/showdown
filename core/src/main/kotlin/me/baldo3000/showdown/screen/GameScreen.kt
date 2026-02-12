package me.baldo3000.showdown.screen

import com.badlogic.gdx.*
import ktx.actors.minusAssign
import ktx.actors.plusAssign
import ktx.ashley.getSystem
import ktx.log.logger
import me.baldo3000.showdown.Showdown
import me.baldo3000.showdown.ecs.system.*
import me.baldo3000.showdown.input.addInputProcessor
import me.baldo3000.showdown.ui.PauseMenuUI
import kotlin.math.min

private const val MAX_DELTA_TIME = 1 / 20f

class GameScreen(game: Showdown) : ShowdownScreen(game) {
    private val menuUI = PauseMenuUI(
        onResume = { closeMenu() },
        onExit = { Gdx.app.exit() }
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
    private var savedInputProcessor: InputProcessor? = null

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
        disableGameSystems()
    }

    override fun render(delta: Float) {
        engine.update(min(delta, MAX_DELTA_TIME))

        if (menuVisible) {
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

    private fun enableGameSystems() {
        engine.run {
            getSystem<CameraSystem>().setProcessing(true)
            getSystem<CollisionSystem>().setProcessing(true)
            getSystem<GameEventsSystem>().setProcessing(true)
            getSystem<MoveSystem>().setProcessing(true)
            getSystem<PlayerInputSystem>().inputEnabled = true
            getSystem<RemoveSystem>().setProcessing(true)
            getSystem<RenderSystem>().setProcessing(true)
        }
    }

    private fun disableGameSystems() {
        engine.run {
            getSystem<CameraSystem>().setProcessing(false)
            getSystem<CollisionSystem>().setProcessing(false)
            getSystem<GameEventsSystem>().setProcessing(false)
            getSystem<MoveSystem>().setProcessing(false)
            getSystem<PlayerInputSystem>().inputEnabled = false
            getSystem<RemoveSystem>().setProcessing(false)
            getSystem<RenderSystem>().setProcessing(false)

            // keep networking running if needed
            getSystem<HostNetworkSystem>().setProcessing(true)
            getSystem<ClientNetworkSystem>().setProcessing(true)
        }
    }

    companion object {
        private val log = logger<GameScreen>()
    }
}
