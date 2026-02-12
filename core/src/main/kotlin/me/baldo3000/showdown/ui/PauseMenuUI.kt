package me.baldo3000.showdown.ui

import com.badlogic.gdx.scenes.scene2d.ui.TextButton
import com.badlogic.gdx.utils.Align
import ktx.actors.onClick
import ktx.scene2d.*

private const val MENU_PADDING = 6f

class PauseMenuUI(
    private val onResume: () -> Unit,
    private val onExit: () -> Unit
) {
    val table: KTableWidget
    val resumeButton: TextButton
    val exitButton: TextButton

    init {
        table = scene2d.table {
            defaults().pad(MENU_PADDING).expandX().fillX()

            label("PAUSED") { cell ->
                wrap = true
                setAlignment(Align.center)
                cell.apply {
                    padTop(20f)
                    padBottom(20f)
                }
            }
            row()

            resumeButton = textButton("Resume")
            row()

            exitButton = textButton("Exit")

            setFillParent(true)
            center()
            pack()
        }

        resumeButton.onClick { onResume() }
        exitButton.onClick { onExit() }
    }
}
