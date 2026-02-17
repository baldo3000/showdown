package me.baldo3000.showdown.ecs.system

import com.badlogic.ashley.core.Entity
import com.badlogic.ashley.systems.IteratingSystem
import ktx.ashley.exclude
import ktx.ashley.get
import ktx.ashley.oneOf
import ktx.log.logger
import me.baldo3000.showdown.ecs.character
import me.baldo3000.showdown.ecs.component.RemoveComponent
import me.baldo3000.showdown.ecs.component.event.EventComponent
import me.baldo3000.showdown.ecs.component.event.PlayerDeathComponent
import me.baldo3000.showdown.ecs.component.event.PlayerJoinComponent
import me.baldo3000.showdown.ecs.component.event.SetupGameComponent
import me.baldo3000.showdown.ecs.createPlayer
import me.baldo3000.showdown.ecs.players
import me.baldo3000.showdown.ecs.spawnWallsFromWorld
import me.baldo3000.showdown.world.ShowdownWorld

class GameEventsSystem : IteratingSystem(
    oneOf(
        PlayerDeathComponent::class,
        PlayerJoinComponent::class,
        SetupGameComponent::class
    ).exclude(RemoveComponent::class).get()
) {
    private val world: ShowdownWorld = ShowdownWorld()
    var onGameEnd: (placement: Int) -> Unit = {}

    override fun setProcessing(processing: Boolean) {
        super.setProcessing(processing)
        if (!processing) world.reset()
    }

    override fun processEntity(entity: Entity, deltaTime: Float) {
        val event: EventComponent = entity[PlayerDeathComponent.mapper]
            ?: entity[PlayerJoinComponent.mapper]
            ?: entity[SetupGameComponent.mapper]
            ?: throw IllegalArgumentException("Entity must be an event")

        processEvent(event)
        entity.add(RemoveComponent())
    }

    private fun processEvent(event: EventComponent) {
        when (event) {
            is PlayerDeathComponent -> {
                if (engine.character == null) {
                    onGameEnd(engine.players.size + 1)
                } else if (engine.players.size == 1) {
                    onGameEnd(1)
                }
            }

            is PlayerJoinComponent -> processPlayerJoinEvent(event)
            is SetupGameComponent -> spawnWalls()
        }
    }

    private fun processPlayerJoinEvent(event: PlayerJoinComponent) {
        if (!world.isGameFull()) {
            val spawnLocation = world.newPlayerSpawnLocation()
            engine.createPlayer(event.playerId, spawnLocation, event.controllable)
        } else {
            log.error { "Cannot spawn new player: game is full" }
        }
    }

    private fun spawnWalls() {
        engine.spawnWallsFromWorld(world)
    }

    companion object {
        private val log = logger<GameEventsSystem>()
    }
}
