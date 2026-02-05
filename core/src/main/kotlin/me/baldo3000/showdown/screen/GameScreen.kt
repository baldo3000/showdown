package me.baldo3000.showdown.screen

import ktx.log.logger
import me.baldo3000.showdown.Showdown
import me.baldo3000.showdown.ecs.createPlayer
import me.baldo3000.showdown.network.Vector2D
import kotlin.math.min
import kotlin.uuid.Uuid

private const val MAX_DELTA_TIME = 1 / 20f

class GameScreen(game: Showdown) : ShowdownScreen(game) {
    private val testShooting = false

    override fun show() {
        log.debug { "GameScreen is shown" }
        if (testShooting) {
            engine.addEntity(engine.createPlayer(Uuid.random(), Vector2D(3f, 3f)))
            engine.addEntity(engine.createPlayer(Uuid.random(), Vector2D(14f, 8f)))
            engine.addEntity(engine.createPlayer(Uuid.random(), Vector2D(14f, 3f)))
            engine.addEntity(engine.createPlayer(Uuid.random(), Vector2D(3f, 8f)))
        }
    }

    override fun render(delta: Float) {
        engine.update(min(delta, MAX_DELTA_TIME))
    }

    override fun dispose() {
        log.debug { "GameScreen has been disposed" }
        super.dispose()
    }

    companion object {
        private val log = logger<GameScreen>()
    }
}
