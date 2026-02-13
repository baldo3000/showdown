package me.baldo3000.showdown.ui

import com.badlogic.gdx.scenes.scene2d.ui.TextButton
import com.badlogic.gdx.utils.Align
import ktx.actors.onClick
import ktx.scene2d.*

private const val OFFSET_TITLE_Y = 15f
private const val MENU_ELEMENT_OFFSET_TITLE_Y = 20f
private const val MENU_DEFAULT_PADDING = 2.5f

class HomeUI(
    private val onHost: () -> Unit = {},
    private val onJoin: () -> Unit = {},
    private val onCredits: () -> Unit = {},
    private val onQuit: () -> Unit = {}
) {
    val table: KTableWidget
    val hostGameButton: TextButton
    val clientGameButton: TextButton
    val creditsButton: TextButton
    val quitGameButton: TextButton

    init {
        table = scene2d.table {
            defaults().pad(MENU_DEFAULT_PADDING).expandX().fillX().colspan(2)

            label("SHOWDOWN") { cell ->
                wrap = true
                setAlignment(Align.center)
                cell.apply {
                    padTop(OFFSET_TITLE_Y)
                    padBottom(MENU_ELEMENT_OFFSET_TITLE_Y)
                }
            }
            row()

            hostGameButton = textButton("Start a game")
            row()

            clientGameButton = textButton("Join a game")
            row()

            creditsButton = textButton("Credits")
            row()

            quitGameButton = textButton("Quit game")

            setFillParent(true)
            top()
            pack()
        }

        hostGameButton.onClick { onHost() }
        clientGameButton.onClick { onJoin() }
        creditsButton.onClick { onCredits() }
        quitGameButton.onClick { onQuit() }
    }
}
