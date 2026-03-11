package me.baldo3000.showdown.ui

import com.badlogic.gdx.scenes.scene2d.ui.Label
import com.badlogic.gdx.scenes.scene2d.ui.TextButton
import com.badlogic.gdx.scenes.scene2d.ui.TextField
import com.badlogic.gdx.utils.Align
import ktx.actors.onClick
import ktx.scene2d.*
import me.baldo3000.showdown.dto.AddressDTO

private const val OFFSET_TITLE_Y = 15f
private const val MENU_ELEMENT_OFFSET_TITLE_Y = 20f
private const val MENU_DEFAULT_PADDING = 2.5f

class HomeUI(
    private val onHost: (Float) -> Unit = {},
    private val onJoin: (String, Int, Float) -> Unit = { _, _, _ -> },
    private val onSearchForServer: () -> Unit = {},
    private val onRefreshLobbies: () -> Unit = {},
    private val onLobbySelected: (String, Int, Float) -> Unit = { _, _, _ -> },
    private val onCredits: () -> Unit = {},
    private val onQuit: () -> Unit = {}
) {
    val table: KTableWidget

    private val hostGameButton: TextButton
    private val ipTextField: TextField
    private val portTextField: TextField
    private val clientGameButton: TextButton
    private val searchForServerButton: TextButton
    private val serverAddressLabel: Label
    private val refreshLobbiesButton: TextButton
    private val creditsButton: TextButton
    private val quitGameButton: TextButton
    private val udpDropRateTextField: TextField

    private val lobbiesContainer: KTableWidget = scene2d.table {
        defaults().pad(MENU_DEFAULT_PADDING).expandX().fillX()
        add(Label("No lobbies found", Scene2DSkin.defaultSkin)).expandX().fillX()
    }

    init {

        table = scene2d.table {
            defaults().pad(MENU_DEFAULT_PADDING).expandX().fillX().colspan(2)

            // Title
            label("SHOWDOWN") { cell ->
                wrap = true
                setAlignment(Align.center)
                cell.padTop(OFFSET_TITLE_Y).padBottom(MENU_ELEMENT_OFFSET_TITLE_Y)
            }
            row()

            // Host
            hostGameButton = textButton("Start a game")
            row()

            label("Host IP:") { cell -> cell.expandX().fillX().colspan(1) }
            ipTextField = textField("127.0.0.1") { cell -> cell.expandX().fillX().colspan(1) }
            row()

            label("Port:") { cell -> cell.expandX().fillX().colspan(1) }
            portTextField = textField("65432") { cell -> cell.expandX().fillX().colspan(1) }
            row()

            // Join
            clientGameButton = textButton("Join a game")
            row()

            // Lobby server discovery
            searchForServerButton = textButton("Search for lobby server") { cell ->
                cell.expandX().fillX().colspan(1)
            }
            serverAddressLabel = label("No server found. (Needed for posting and searching lobbies)") { cell ->
                cell.expandX().fillX().colspan(1)
            }
            row()

            refreshLobbiesButton = textButton("Refresh lobbies")
            row()

            // Lobbies list
            add(lobbiesContainer).expandX().fillX().colspan(2)
            row()

            // Credits
            creditsButton = textButton("Credits")
            row()

            // Quit
            quitGameButton = textButton("Quit game")
            row()

            // Vertical spacer
            add().expandY().colspan(2)
            row()

            // UDP drop-rate: label and field nested together so they sit flush
            add(scene2d.table {
                label("Game UDP Send Drop Rate (0.0 to 1.0): ") { cell ->
                    cell.padRight(MENU_DEFAULT_PADDING)
                }
                udpDropRateTextField = textField("0.0") { cell ->
                    cell.width(80f)
                }
                left()
            }).colspan(2).expandX().left().pad(MENU_DEFAULT_PADDING)

            setFillParent(true)
            top()
            pack()
        }

        hostGameButton.onClick { onHost(udpDropRateTextField.text.toFloatOrNull() ?: 0f) }
        clientGameButton.onClick {
            val ip = ipTextField.text.trim()
            val port = portTextField.text.toIntOrNull() ?: 65432
            onJoin(ip, port, udpDropRateTextField.text.toFloatOrNull() ?: 0f)
        }
        searchForServerButton.onClick { onSearchForServer() }
        refreshLobbiesButton.onClick { onRefreshLobbies() }
        creditsButton.onClick { onCredits() }
        quitGameButton.onClick { onQuit() }
    }

    fun updateServerAddress(text: String) {
        serverAddressLabel.setText(text)
    }

    fun updateLobbies(lobbies: List<AddressDTO>) {
        lobbiesContainer.clearChildren()
        if (lobbies.isEmpty()) {
            lobbiesContainer.add(Label("No lobbies found", Scene2DSkin.defaultSkin))
                .expandX().fillX().pad(MENU_DEFAULT_PADDING)
        } else {
            lobbiesContainer.add(Label("Available lobbies (click to join):", Scene2DSkin.defaultSkin))
                .expandX().fillX().pad(MENU_DEFAULT_PADDING)
            lobbiesContainer.row()
            for (lobby in lobbies) {
                val btn = TextButton("${lobby.ip}:${lobby.port}", Scene2DSkin.defaultSkin)
                btn.onClick { onLobbySelected(lobby.ip, lobby.port, udpDropRateTextField.text.toFloatOrNull() ?: 0f) }
                lobbiesContainer.add(btn).expandX().fillX().pad(MENU_DEFAULT_PADDING)
                lobbiesContainer.row()
            }
        }
        lobbiesContainer.invalidateHierarchy()
    }

    fun setLobbiesStatus(message: String) {
        lobbiesContainer.clearChildren()
        lobbiesContainer.add(Label(message, Scene2DSkin.defaultSkin))
            .expandX().fillX().pad(MENU_DEFAULT_PADDING)
        lobbiesContainer.invalidateHierarchy()
    }
}
