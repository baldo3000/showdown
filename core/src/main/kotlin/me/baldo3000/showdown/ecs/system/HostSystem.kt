package me.baldo3000.showdown.ecs.system

import com.badlogic.ashley.core.Engine
import com.badlogic.ashley.systems.IntervalSystem
import com.badlogic.gdx.Gdx
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json
import ktx.ashley.entity
import ktx.ashley.get
import ktx.ashley.with
import ktx.log.logger
import me.baldo3000.showdown.data.Vector2D
import me.baldo3000.showdown.ecs.bullets
import me.baldo3000.showdown.ecs.component.IdComponent
import me.baldo3000.showdown.ecs.component.MoveComponent
import me.baldo3000.showdown.ecs.component.RemoveComponent
import me.baldo3000.showdown.ecs.component.TransformComponent
import me.baldo3000.showdown.ecs.component.event.CheckGameEndComponent
import me.baldo3000.showdown.ecs.players
import me.baldo3000.showdown.ecs.walls
import me.baldo3000.showdown.game.EntityFactory
import me.baldo3000.showdown.game.GameState
import me.baldo3000.showdown.network.HostNetworkManager
import me.baldo3000.showdown.network.NetworkConfig
import me.baldo3000.showdown.network.PlayerInputPacket
import me.baldo3000.showdown.network.WorldSnapshot
import kotlin.uuid.Uuid

private const val UPDATE_RATE = 1 / 60f

class HostSystem(
    private val networkConfig: NetworkConfig,
    private val entityFactory: EntityFactory,
    private val gameState: GameState
) : IntervalSystem(UPDATE_RATE) {
    private val networkManager: HostNetworkManager
    private var snapshotSequenceNumber = 0
    private var sessionId: Uuid = Uuid.random()

    private val playerLastInputSequenceNumbers = mutableMapOf<Uuid, Int>()

    private val speedVector = Vector2D()
    private val bulletSpeedVector = Vector2D()

    init {
        networkManager = HostNetworkManager(
            onPeerConnect = { peerId ->
                Gdx.app.postRunnable {
                    if (gameState.isGameFull() || gameState.inputEnabled) {
                        networkManager.disconnectClient(peerId)
                    } else {
                        entityFactory.createPlayer(peerId, gameState.newPlayerSpawnLocation(), false)
                        playerLastInputSequenceNumbers[peerId] = -1
                    }
                }
            },
            onPeerDisconnect = { peerId ->
                Gdx.app.postRunnable {
                    for (entity in engine.players) {
                        entity[IdComponent.mapper]?.id?.let { id ->
                            if (id == peerId) {
                                playerLastInputSequenceNumbers.remove(peerId)
                                entity.add(RemoveComponent())
                                engine.entity { with<CheckGameEndComponent>() }
                            }
                        }
                    }
                }
            })
    }

    override fun setProcessing(processing: Boolean) {
        super.setProcessing(processing)
        if (processing) {
            snapshotSequenceNumber = 0
            playerLastInputSequenceNumbers.clear()
            sessionId = Uuid.random()
            entityFactory.createPlayer(Uuid.random(), gameState.newPlayerSpawnLocation(), true)
            entityFactory.createWallsFromMapSize(gameState.mapSize)
            networkManager.setUDPDropRate(networkConfig.udpDropRate)
            networkManager.start()
        } else {
            Gdx.graphics.setTitle("Showdown")
            reset()
        }
    }

    override fun removedFromEngine(engine: Engine?) {
        super.removedFromEngine(engine)
        reset()
    }

    override fun updateInterval() {
        Gdx.graphics.setTitle(
            "Showdown - Host listening on addresses ${networkManager.addresses}, port ${networkManager.port ?: "Unknown Port"} - Players: ${engine.players.size}"
        )
        processIncomingMessages()
        broadcastWorldState()
    }

    private fun broadcastWorldState() {
        // log.debug { "Sending broadcast update: $worldSnapshot" }
        val worldSnapshot =
            WorldSnapshot.fromEntities(
                engine.players,
                engine.bullets,
                engine.walls,
                sessionId,
                snapshotSequenceNumber++
            )
        val bytes = Json.encodeToString(worldSnapshot).toByteArray()
        networkManager.sendToClients(bytes)
    }

    private fun processIncomingMessages() {
        while (true) {
            val packet = networkManager.receiveChannel.tryReceive().getOrNull() ?: break
            try {
                val playerInput = Json.decodeFromString<PlayerInputPacket>(packet.decodeToString())
                processPlayerInput(playerInput)
            } catch (_: IllegalArgumentException) {
                log.error { "A packet coming from client was discarded because it's not a PlayerInputPacket" }
            } catch (_: SerializationException) {
                log.error { "Unknown error during deserialization of packet coming from client, discarding packet" }
            }
        }
    }

    private fun processPlayerInput(playerInput: PlayerInputPacket) {
        if (!gameState.inputEnabled) return
        // log.debug { "Processing input packet: $playerInput" }
        val lastSequenceNumber = playerLastInputSequenceNumbers[playerInput.id]
        if (lastSequenceNumber != null) {
            if (playerInput.sequenceNumber > lastSequenceNumber) {
                playerLastInputSequenceNumbers[playerInput.id] = playerInput.sequenceNumber
                speedVector.set(playerInput.horizontal.toFloat(), playerInput.vertical.toFloat()).nor()
                engine.entities.forEach {
                    val id = it[IdComponent.mapper] ?: return@forEach
                    val move = it[MoveComponent.mapper] ?: return@forEach
                    val transform = it[TransformComponent.mapper] ?: return@forEach
                    if (id.id == playerInput.id) {
                        move.speed.x = speedVector.x * 3f
                        move.speed.y = speedVector.y * 3f
                        if (playerInput.touching != null) {
                            bulletSpeedVector.set(
                                playerInput.touching.x - transform.position.x,
                                playerInput.touching.y - transform.position.y
                            ).nor()
                            entityFactory.createBullet(
                                bulletId = Uuid.random(),
                                sourceId = id.id,
                                position = transform.position.to2D(),
                                speed = Vector2D(bulletSpeedVector.x * 5f, bulletSpeedVector.y * 5f)
                            )
                        }
                    }
                }
            } else {
                log.error { "Discard input packet out of sequence" }
            }
        }
    }

    private fun reset() {
        networkManager.stop()
    }

    companion object {
        private val log = logger<HostSystem>()
    }
}
