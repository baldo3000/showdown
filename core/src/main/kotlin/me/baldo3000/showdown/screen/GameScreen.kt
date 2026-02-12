package me.baldo3000.showdown.screen

import ktx.ashley.getSystem
import ktx.log.logger
import me.baldo3000.showdown.Showdown
import me.baldo3000.showdown.ecs.system.*
import kotlin.math.min

private const val MAX_DELTA_TIME = 1 / 20f

class GameScreen(game: Showdown) : ShowdownScreen(game) {
    override fun show() {
        super.show()
        log.debug { "GameScreen is shown" }
        enableGameSystems()
    }

    override fun hide() {
        super.hide()
        disableGameSystems()
    }

    override fun render(delta: Float) {
        engine.update(min(delta, MAX_DELTA_TIME))
    }

    override fun dispose() {
        log.debug { "GameScreen has been disposed" }
        super.dispose()
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

            getSystem<HostNetworkSystem>().setProcessing(true)
            getSystem<ClientNetworkSystem>().setProcessing(true)
        }
    }

    companion object {
        private val log = logger<GameScreen>()
    }
}
