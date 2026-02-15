package me.baldo3000.showdown.ui

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.scenes.scene2d.ui.Label
import com.badlogic.gdx.scenes.scene2d.ui.TextButton
import com.badlogic.gdx.utils.Align
import ktx.actors.onClick
import ktx.scene2d.*

private const val MENU_PADDING = 6f

class GameEndUI(
    private val onClose: () -> Unit
) {
    val table: KTableWidget
    private val titleLabel: Label
    private val closeButton: TextButton

    init {
        table = scene2d.table {
            defaults().pad(MENU_PADDING).expandX().fillX()

            titleLabel = label("GAME ENDED") { cell ->
                wrap = true
                setAlignment(Align.center)
                cell.apply {
                    padTop(20f)
                    padBottom(20f)
                }
            }
            row()

            closeButton = textButton("Close")

            setFillParent(true)
            center()
            pack()
            Gdx.input.isKeyJustPressed(1)
        }

        closeButton.onClick { onClose() }
    }

    fun setPlacement(placement: Int) {
        val placementText = when (placement) {
            1 -> "1st Place - Victory!"
            2 -> "2nd Place - Defeat!"
            3 -> "3rd Place - Defeat!"
            else -> "${placement}th Place - Defeat!"
        }
        titleLabel.setText(placementText)
    }

    fun setDisconnected() {
        titleLabel.setText("You have been disconnected from the host.")
    }
}
