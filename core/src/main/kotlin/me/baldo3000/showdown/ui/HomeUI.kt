package me.baldo3000.showdown.ui

import com.badlogic.gdx.scenes.scene2d.ui.TextButton
import com.badlogic.gdx.scenes.scene2d.ui.TextField
import com.badlogic.gdx.utils.Align
import ktx.actors.onClick
import ktx.scene2d.*

private const val OFFSET_TITLE_Y = 15f
private const val MENU_ELEMENT_OFFSET_TITLE_Y = 20f
private const val MENU_DEFAULT_PADDING = 2.5f

class HomeUI(
    private val onHost: (Float) -> Unit = {},
    private val onJoin: (String, Int, Float) -> Unit = { _, _, _ -> },
    private val onCredits: () -> Unit = {},
    private val onQuit: () -> Unit = {}
) {
    val table: KTableWidget
    private val hostGameButton: TextButton
    private val ipTextField: TextField
    private val portTextField: TextField
    private val clientGameButton: TextButton
    private val creditsButton: TextButton
    private val quitGameButton: TextButton

    val udpDropRateTable: KTableWidget
    private val udpDropRateTextField: TextField

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

            label("Host IP:") { cell ->
                cell.expandX().fillX().colspan(1)
            }
            ipTextField = textField("127.0.0.1") { cell ->
                cell.expandX().fillX().colspan(1)
            }
            row()

            label("Port:") { cell ->
                cell.expandX().fillX().colspan(1)
            }
            portTextField = textField("8080") { cell ->
                cell.expandX().fillX().colspan(1)
            }
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

        udpDropRateTable = scene2d.table {
            label("UDP Send Drop Rate (0.0 to 1.0):") { cell ->
                cell.padRight(MENU_DEFAULT_PADDING)
            }
            udpDropRateTextField = textField("0") { cell ->
                cell.width(80f)
            }

            setFillParent(true)
            bottom().right().pad(10f)
            pack()
        }

        hostGameButton.onClick { onHost(udpDropRateTextField.text.toFloatOrNull() ?: 0f) }
        clientGameButton.onClick {
            val ip = ipTextField.text.trim()
            val port = portTextField.text.toIntOrNull() ?: 8080
            onJoin(ip, port, udpDropRateTextField.text.toFloatOrNull() ?: 0f)
        }
        creditsButton.onClick { onCredits() }
        quitGameButton.onClick { onQuit() }
    }
}
