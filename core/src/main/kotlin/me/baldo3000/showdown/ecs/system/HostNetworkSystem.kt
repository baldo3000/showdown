package me.baldo3000.showdown.ecs.system

import com.badlogic.ashley.core.Engine
import com.badlogic.ashley.core.Entity
import com.badlogic.ashley.systems.IntervalSystem
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.math.Vector2
import kotlinx.serialization.json.Json
import ktx.ashley.get
import ktx.log.logger
import me.baldo3000.showdown.ecs.*
import me.baldo3000.showdown.ecs.component.*
import me.baldo3000.showdown.event.GameEventHandler
import me.baldo3000.showdown.network.*
import me.baldo3000.showdown.world.ShowdownWorld
import network.HostNetworkManager
import kotlin.uuid.Uuid

private const val UPDATE_RATE = 1 / 30f

class HostNetworkSystem(
    port: Int,
    private val eventHandler: GameEventHandler
) : IntervalSystem(UPDATE_RATE) {
    private val networkManager: HostNetworkManager
    private var snapshotSequenceNumber = 0
    private val playerEntities = mutableMapOf<Uuid, Entity>()
    private val playerLastInputSequenceNumbers = mutableMapOf<Uuid, Int>()

    init {
        loadSerializers()
        networkManager = HostNetworkManager(
            port,
            onPeerConnect = { peerId ->
                Gdx.app.postRunnable {
                    val player = engine.createPlayer(peerId, controllable = false)
                    playerEntities[peerId] = player
                    playerLastInputSequenceNumbers[peerId] = -1
                }
            },
            onPeerDisconnect = { peerId ->
                Gdx.app.postRunnable {
                    playerEntities[peerId]?.let { entity ->
                        playerEntities.remove(peerId)
                        playerLastInputSequenceNumbers.remove(peerId)
                        entity.add(RemoveComponent())
                    }
                }
            })
    }

    override fun addedToEngine(engine: Engine) {
        super.addedToEngine(engine)
        engine.createPlayer(Uuid.random(), controllable = true)
        engine.initializeWorld(ShowdownWorld())
        networkManager.start()
    }

    override fun removedFromEngine(engine: Engine) {
        super.removedFromEngine(engine)
        networkManager.stop()
    }

    override fun updateInterval() {
        processIncomingMessages()
        broadcastWorldState()
    }

    private fun broadcastWorldState() {
        // log.debug { "Sending broadcast update: $worldSnapshot" }
        val worldSnapshot =
            WorldSnapshot.fromEntities(engine.players, engine.bullets, engine.walls, snapshotSequenceNumber++)
        val bytes = Json.encodeToString(worldSnapshot).toByteArray()
        networkManager.sendToClients(bytes)
    }

    private fun processIncomingMessages() {
        while (true) {
            val packet = networkManager.receiveChannel.tryReceive().getOrNull() ?: break
            val playerInput = Json.decodeFromString<PlayerInputPacket>(packet.decodeToString())
            processPlayerInput(playerInput)
        }
    }

    private fun processPlayerInput(playerInput: PlayerInputPacket) {
        val lastSequenceNumber = playerLastInputSequenceNumbers[playerInput.id]
        if (lastSequenceNumber != null) {
            if (playerInput.sequenceNumber > lastSequenceNumber) {
                playerLastInputSequenceNumbers[playerInput.id] = playerInput.sequenceNumber
                val speedVector = Vector2(playerInput.horizontal.toFloat(), playerInput.vertical.toFloat()).nor()
                engine.entities.forEach {
                    val id = it[IdComponent.mapper] ?: return@forEach
                    val move = it[MoveComponent.mapper] ?: return@forEach
                    val transform = it[TransformComponent.mapper] ?: return@forEach
                    if (id.id == playerInput.id) {
                        move.speed.x = speedVector.x * 3f
                        move.speed.y = speedVector.y * 3f
                        if (playerInput.touching != null) {
                            val distX = playerInput.touching.x - transform.position.x
                            val distY = playerInput.touching.y - transform.position.y
                            val bulletSpeedVector = Vector2(distX, distY).nor()
                            engine.createBullet(
                                Uuid.random(),
                                id.id,
                                DEFAULT_DAMAGE,
                                Vector2D(transform.position.x, transform.position.y),
                                Vector2D(bulletSpeedVector.x * 5f, bulletSpeedVector.y * 5f)
                            )
                        }
                    }
                }
            } else {
                log.error { "Discard input packet out of sequence" }
            }
        }
    }

    private fun loadSerializers() {
        PlayerInputPacket.serializer()
        PlayerSnapshot.serializer()
        BulletSnapshot.serializer()
        WorldSnapshot.serializer()
    }

    companion object {
        private val log = logger<HostNetworkSystem>()
    }
}
