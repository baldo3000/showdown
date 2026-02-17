package me.baldo3000.showdown.ui

import com.badlogic.gdx.scenes.scene2d.ui.TextButton
import ktx.actors.onClick
import ktx.scene2d.KTableWidget
import ktx.scene2d.scene2d
import ktx.scene2d.table
import ktx.scene2d.textButton

class HostControlUI(
    private val onStartGame: () -> Unit
) {
    val table: KTableWidget
    private val startButton: TextButton

    init {
        table = scene2d.table {
            startButton = textButton("Start game")
            setFillParent(true)
            center()
            pack()
        }

        startButton.onClick { onStartGame() }
    }
}
