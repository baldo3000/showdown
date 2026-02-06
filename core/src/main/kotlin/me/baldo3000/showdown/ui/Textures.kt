package me.baldo3000.showdown.ui

import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.Pixmap
import com.badlogic.gdx.graphics.Texture

object Textures {
    private const val PLAYER_TEXTURE_SIZE = 64
    private const val BULLET_TEXTURE_SIZE = 4
    private const val WALL_TEXTURE_SIZE = 1

    val playerTexture: Texture
    val bulletTexture: Texture
    val wallTexture: Texture

    init {
        val playerPixmap = Pixmap(PLAYER_TEXTURE_SIZE, PLAYER_TEXTURE_SIZE, Pixmap.Format.RGBA8888).apply {
            setColor(Color.CYAN)
            fillCircle(PLAYER_TEXTURE_SIZE / 2, PLAYER_TEXTURE_SIZE / 2, PLAYER_TEXTURE_SIZE / 2 - 1)
        }
        val bulletPixmap = Pixmap(BULLET_TEXTURE_SIZE, BULLET_TEXTURE_SIZE, Pixmap.Format.RGBA8888).apply {
            setColor(Color.RED)
            fillCircle(BULLET_TEXTURE_SIZE / 2, BULLET_TEXTURE_SIZE / 2, BULLET_TEXTURE_SIZE / 2 - 1)
        }
        val wallPixmap = Pixmap(WALL_TEXTURE_SIZE, WALL_TEXTURE_SIZE, Pixmap.Format.RGBA8888).apply {
            setColor(Color.TAN)
            fillRectangle(0, 0, WALL_TEXTURE_SIZE, WALL_TEXTURE_SIZE)
        }

        playerTexture = Texture(playerPixmap)
        bulletTexture = Texture(bulletPixmap)
        wallTexture = Texture(wallPixmap)

        playerPixmap.dispose()
        bulletPixmap.dispose()
        wallPixmap.dispose()
    }

    fun dispose() {
        playerTexture.dispose()
        bulletTexture.dispose()
        wallTexture.dispose()
    }
}
