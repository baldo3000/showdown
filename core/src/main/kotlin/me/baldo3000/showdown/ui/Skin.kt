package me.baldo3000.showdown.ui

import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.Pixmap
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.BitmapFont
import com.badlogic.gdx.scenes.scene2d.ui.Skin
import ktx.scene2d.Scene2DSkin
import ktx.style.label
import ktx.style.skin
import ktx.style.textButton
import ktx.style.textField

fun createSkin() {
    Scene2DSkin.defaultSkin = skin { skin ->
        createBackgroundTexture()
        createFontStyles()
        createLabelStyles()
        createTextButtonStyles()
        createTextFieldStyles()
    }
}

private fun Skin.createBackgroundTexture() {
    val pixmap = Pixmap(1, 1, Pixmap.Format.RGBA8888)
    pixmap.setColor(Color.WHITE)
    pixmap.fill()
    val texture = Texture(pixmap)
    // pixmap can be disposed immediately; texture retains the pixel data
    pixmap.dispose()
    add("background", texture)
}

private fun Skin.createFontStyles() {
    add("default", BitmapFont())
}

private fun Skin.createTextButtonStyles() {
    textButton("default") {
        font = getFont("default")
        up = newDrawable("background", Color.GRAY)
        down = newDrawable("background", Color.DARK_GRAY)
        over = newDrawable("background", Color.LIGHT_GRAY)
    }
}

private fun Skin.createLabelStyles() {
    label("default") {
        font = getFont("default")
    }
}

private fun Skin.createTextFieldStyles() {
    textField("default") {
        font = getFont("default")
        fontColor = Color.WHITE
        background = newDrawable("background", Color.DARK_GRAY)
        focusedBackground = newDrawable("background", Color.GRAY)
        cursor = newDrawable("background", Color.WHITE)
        selection = newDrawable("background", Color.LIGHT_GRAY)
    }
}
