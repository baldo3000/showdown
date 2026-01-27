package me.baldo3000.showdown.screen

import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.Pixmap
import com.badlogic.gdx.graphics.Texture
import ktx.ashley.entity
import ktx.ashley.with
import ktx.log.logger
import me.baldo3000.showdown.Showdown
import me.baldo3000.showdown.UNIT_SCALE
import me.baldo3000.showdown.ecs.component.*
import kotlin.math.min

private const val MAX_DELTA_TIME = 1 / 20f

class GameScreen(game: Showdown) : ShowdownScreen(game) {

    override fun show() {
        log.debug { "GameScreen is shown" }

        val size = 32
        val redPixmap = Pixmap(size, size, Pixmap.Format.RGBA8888).apply {
            setColor(0f, 0f, 0f, 0f)
            fill()
            // draw filled circle centered in pixmap
            setColor(Color.RED)
            fillCircle(size / 2, size / 2, size / 2 - 1)

        }
        val redTexture = Texture(redPixmap)
        redPixmap.dispose()
        engine.entity {
            with<TransformComponent> {
                position.set(8f, 4.5f, 0f)
            }
            with<GraphicComponent> {
                sprite.run {
                    setRegion(redTexture)
                    setSize(texture.width * UNIT_SCALE, texture.height * UNIT_SCALE)
                    setOriginCenter()
                }
            }
            with<FacingComponent> {}
            with<PlayerComponent> {}
            with<MoveComponent> {}
            with<IdComponent> {}
        }
    }

    override fun render(delta: Float) {
        engine.update(min(delta, MAX_DELTA_TIME))
    }

    companion object {
        private val log = logger<GameScreen>()
    }
}
