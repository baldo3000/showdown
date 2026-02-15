package me.baldo3000.showdown.ui

import com.badlogic.gdx.scenes.scene2d.ui.Label
import com.badlogic.gdx.scenes.scene2d.ui.TextButton
import com.badlogic.gdx.utils.Align
import ktx.actors.onClick
import ktx.scene2d.*

class HostControlUI(
    private val onStartGame: () -> Unit
) {
    val table: KTableWidget
    private val hostingLabel: Label
    private val startButton: TextButton

    init {
        table = scene2d.table {
            hostingLabel = label("") { cell ->
                wrap = true
                setAlignment(Align.center)
                cell.apply {
                    padTop(20f)
                    padBottom(20f)
                }
            }
            row()

            startButton = textButton("Start game")

            setFillParent(true)
            center()
            pack()
        }

        startButton.onClick { onStartGame() }
    }

    fun updateHostingInfo(info: String) {
        hostingLabel.setText(info)
    }
}
