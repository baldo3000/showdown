package me.baldo3000.showdown.ecs.system

import com.badlogic.ashley.core.Engine
import com.badlogic.ashley.core.Entity
import com.badlogic.ashley.systems.IntervalSystem
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Input
import com.badlogic.gdx.utils.viewport.Viewport
import kotlinx.serialization.json.Json
import ktx.ashley.get
import ktx.log.logger
import me.baldo3000.showdown.data.Vector2D
import me.baldo3000.showdown.ecs.component.*
import me.baldo3000.showdown.ecs.createBullet
import me.baldo3000.showdown.ecs.createPlayer
import me.baldo3000.showdown.ecs.createWall
import me.baldo3000.showdown.ecs.reset
import me.baldo3000.showdown.ecs.spawnPlayer
import me.baldo3000.showdown.ecs.spawnWalls
import me.baldo3000.showdown.input.DummyInputProcessor
import me.baldo3000.showdown.input.addInputProcessor
import me.baldo3000.showdown.input.removeInputProcessor
import me.baldo3000.showdown.network.PlayerInputPacket
import me.baldo3000.showdown.network.WorldSnapshot
import network.ClientNetworkManager
import kotlin.uuid.Uuid

private const val UPDATE_RATE = 1 / 30f

class ClientNetworkSystem(
    private val gameViewport: Viewport
) : IntervalSystem(UPDATE_RATE), DummyInputProcessor {
    private val networkManager: ClientNetworkManager
    private val idMap = mutableMapOf<Uuid, Entity>()
    private var inputSequenceNumber = 0
    private var lastSnapshotSequenceNumber = -1
    private var connected = false
    private var playerEntity: Entity? = null

    private var tmpShootVector: Vector2D? = null
    private var horizontal = 0
    private var vertical = 0

    init {
        networkManager = ClientNetworkManager(onConnect = {
            Gdx.app.postRunnable { connected = true }
        })
    }

    override fun setProcessing(processing: Boolean) {
        super.setProcessing(processing)
        if (processing) {
            addInputProcessor(this)
            networkManager.connect("127.0.0.1", 8080)
        } else {
            removeInputProcessor(this)
            networkManager.stop()
        }
    }

    override fun removedFromEngine(engine: Engine) {
        super.removedFromEngine(engine)
        networkManager.stop()
    }

    override fun updateInterval() {
        processIncomingMessages()
    }

    private fun processIncomingMessages() {
        //var latestPacket: ByteArray? = null
        val latestPacket = generateSequence {
            networkManager.receiveChannel.tryReceive().getOrNull()
        }.lastOrNull()

        if (latestPacket != null) {
            val worldState = Json.decodeFromString<WorldSnapshot>(latestPacket.decodeToString())
            // log.debug { "Received world update: $worldState" }
            syncWorld(worldState)
        }
    }

    private fun syncWorld(state: WorldSnapshot) {
        if (state.sequenceNumber > lastSnapshotSequenceNumber) {
            lastSnapshotSequenceNumber = state.sequenceNumber
            val serverIds = state.players.map { it.id } + state.bullets.map { it.id } + state.walls.map { it.id }
            for (entity in engine.entities) {
                val id = entity[IdComponent.mapper]?.id
                if (id !in serverIds) {
                    entity.add(RemoveComponent())
                    idMap.remove(id)
                }
            }
            state.players.forEach { snapshot ->
                val entity = idMap.getOrPut(snapshot.id) {
                    val newPlayer = engine.createPlayer(
                        snapshot.id,
                        snapshot.position,
                        snapshot.id == networkManager.id
                    )
                    if (networkManager.id == snapshot.id) playerEntity = newPlayer
                    newPlayer
                }
                // log.debug { "Updating player with id ${snapshot.id}" }
                entity[TransformComponent.mapper]?.position?.let {
                    it.set(snapshot.position.x, snapshot.position.y, it.z)
                }
                entity[MoveComponent.mapper]?.speed?.set(snapshot.speed.x, snapshot.speed.y)
                entity[HealthComponent.mapper]?.health = snapshot.health
            }

            state.bullets.forEach { snapshot ->
                val entity = idMap.getOrPut(snapshot.id) {
                    val newBullet = engine.createBullet(
                        snapshot.id,
                        snapshot.sourceId,
                        snapshot.damage,
                        snapshot.position,
                        snapshot.speed
                    )
                    newBullet
                }
                // log.debug { "Updating bullet with id ${snapshot.id}" }
                entity[TransformComponent.mapper]?.position?.let {
                    it.set(snapshot.position.x, snapshot.position.y, it.z)
                }
                entity[MoveComponent.mapper]?.speed?.set(snapshot.speed.x, snapshot.speed.y)
            }

            state.walls.forEach { snapshot ->
                idMap.getOrPut(snapshot.id) {
                    val newWall = engine.createWall(
                        snapshot.id,
                        snapshot.position,
                        snapshot.size
                    )
                    newWall
                }
            }
        } else {
            log.error { "Discard input packet out of sequence" }
        }
    }

    private fun sendPlayerInput() {
        networkManager.id?.let { id ->
            playerEntity?.let {
                val inputPacket = PlayerInputPacket(
                    id,
                    horizontal,
                    vertical,
                    tmpShootVector,
                    inputSequenceNumber++
                )
                val bytes = Json.encodeToString(inputPacket).toByteArray()
                // log.debug { "Sending player info to host: $inputPacket" }
                networkManager.sendToHost(bytes)
            }
        }
    }

    override fun keyDown(keycode: Int): Boolean {
        when (keycode) {
            Input.Keys.W, Input.Keys.UP -> vertical += 1
            Input.Keys.S, Input.Keys.DOWN -> vertical -= 1
            Input.Keys.A, Input.Keys.LEFT -> horizontal -= 1
            Input.Keys.D, Input.Keys.RIGHT -> horizontal += 1
        }
        sendPlayerInput()
        return super.keyDown(keycode)
    }

    override fun keyUp(keycode: Int): Boolean {
        when (keycode) {
            Input.Keys.W, Input.Keys.UP -> vertical -= 1
            Input.Keys.S, Input.Keys.DOWN -> vertical += 1
            Input.Keys.A, Input.Keys.LEFT -> horizontal += 1
            Input.Keys.D, Input.Keys.RIGHT -> horizontal -= 1
        }
        sendPlayerInput()
        return super.keyUp(keycode)
    }

    override fun touchDown(screenX: Int, screenY: Int, pointer: Int, button: Int): Boolean {
        tmpShootVector = Vector2D(screenX.toFloat(), screenY.toFloat())

        gameViewport.unproject(tmpShootVector)
        sendPlayerInput()
        return super.touchDown(screenX, screenY, pointer, button)
    }

    override fun touchUp(screenX: Int, screenY: Int, pointer: Int, button: Int): Boolean {
        tmpShootVector = null
        sendPlayerInput()
        return super.touchUp(screenX, screenY, pointer, button)
    }

    companion object {
        private val log = logger<ClientNetworkSystem>()
    }
}
