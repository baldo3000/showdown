package me.baldo3000.showdown.ecs.system

import com.badlogic.ashley.core.Engine
import com.badlogic.ashley.core.Entity
import com.badlogic.ashley.systems.IntervalSystem
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Input
import kotlinx.serialization.json.Json
import ktx.ashley.get
import ktx.log.logger
import me.baldo3000.showdown.ecs.component.HealthComponent
import me.baldo3000.showdown.ecs.component.MoveComponent
import me.baldo3000.showdown.ecs.component.RemoveComponent
import me.baldo3000.showdown.ecs.component.TransformComponent
import me.baldo3000.showdown.ecs.createBullet
import me.baldo3000.showdown.ecs.createPlayer
import me.baldo3000.showdown.event.GameEventHandler
import me.baldo3000.showdown.network.PlayerInputPacket
import me.baldo3000.showdown.network.Vector2D
import me.baldo3000.showdown.network.WorldSnapshot
import network.ClientNetworkManager
import kotlin.uuid.Uuid

private const val UPDATE_RATE = 1 / 30f

class ClientNetworkSystem(
    private val eventHandler: GameEventHandler
) : IntervalSystem(UPDATE_RATE) {
    private val networkManager: ClientNetworkManager
    private val idMap = mutableMapOf<Uuid, Entity>()
    private var inputSequenceNumber = 0
    private var lastSnapshotSequenceNumber = -1
    private var connected = false
    private var playerEntity: Entity? = null

    init {
        networkManager = ClientNetworkManager(onConnect = {
            Gdx.app.postRunnable { connected = true }
        })
    }

    override fun addedToEngine(engine: Engine?) {
        super.addedToEngine(engine)
        networkManager.connect("127.0.0.1", 8080)
    }

    override fun removedFromEngine(engine: Engine?) {
        super.removedFromEngine(engine)
        networkManager.stop()
    }

    override fun updateInterval() {
        processIncomingMessages()
        sendPlayerInputPacket()
    }

    private fun sendPlayerInputPacket() {
        networkManager.id?.let { id ->
            playerEntity?.let { entity ->
                val top = Gdx.input.isKeyPressed(Input.Keys.W) || Gdx.input.isKeyPressed(Input.Keys.UP)
                val left = Gdx.input.isKeyPressed(Input.Keys.A) || Gdx.input.isKeyPressed(Input.Keys.LEFT)
                val bottom = Gdx.input.isKeyPressed(Input.Keys.S) || Gdx.input.isKeyPressed(Input.Keys.DOWN)
                val right = Gdx.input.isKeyPressed(Input.Keys.D) || Gdx.input.isKeyPressed(Input.Keys.RIGHT)

                val horizontal = (if (right) 1 else 0) - (if (left) 1 else 0)
                val vertical = (if (top) 1 else 0) - (if (bottom) 1 else 0)
                val inputPacket = PlayerInputPacket(id, horizontal, vertical, inputSequenceNumber++)
                val bytes = Json.encodeToString(inputPacket).toByteArray()
                // log.debug { "Sending player info to host: $inputPacket" }
                networkManager.sendToHost(bytes)
            }
        }
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
            val serverIds = state.players.map { it.id } + state.bullets.map { it.id }
            // log.debug { serverIds.toString() }
            val iterator = idMap.iterator()
            while (iterator.hasNext()) {
                val entry = iterator.next()
                if (entry.key !in serverIds) {
                    log.debug { "Removing entity with id ${entry.key}" }
                    entry.value.add(RemoveComponent())
                    iterator.remove()
                }
            }
            state.players.forEach { snapshot ->
                val entity = idMap.getOrPut(snapshot.id) {
                    val newPlayer = engine.createPlayer(
                        snapshot.id,
                        Vector2D(snapshot.position.x, snapshot.position.y),
                        snapshot.id == networkManager.id
                    )
                    playerEntity = newPlayer
                    engine.addEntity(newPlayer)
                    newPlayer
                }
                // log.debug { "Updating entity with id ${snapshot.id}" }
                entity[TransformComponent.mapper]?.position?.set(snapshot.position.x, snapshot.position.y, 0f)
                entity[MoveComponent.mapper]?.speed?.set(snapshot.speed.x, snapshot.speed.y)
                entity[HealthComponent.mapper]?.health = snapshot.health
            }

            state.bullets.forEach { snapshot ->
                val entity = idMap.getOrPut(snapshot.id) {
                    val newPlayer = engine.createBullet(
                        snapshot.id,
                        snapshot.sourceId,
                        snapshot.damage,
                        Vector2D(snapshot.position.x, snapshot.position.y),
                        Vector2D(snapshot.speed.x, snapshot.speed.y)
                    )
                    playerEntity = newPlayer
                    engine.addEntity(newPlayer)
                    newPlayer
                }
                // log.debug { "Updating entity with id ${snapshot.id}" }
                entity[TransformComponent.mapper]?.position?.set(snapshot.position.x, snapshot.position.y, 0f)
                entity[MoveComponent.mapper]?.speed?.set(snapshot.speed.x, snapshot.speed.y)
            }
        } else {
            log.error { "Discard input packet out of sequence" }
        }
    }

    companion object {
        private val log = logger<ClientNetworkSystem>()
    }
}
