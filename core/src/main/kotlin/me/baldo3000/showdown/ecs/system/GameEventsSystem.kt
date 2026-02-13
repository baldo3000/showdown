package me.baldo3000.showdown.ecs.system

import com.badlogic.ashley.core.Entity
import com.badlogic.ashley.systems.IteratingSystem
import ktx.ashley.exclude
import ktx.ashley.get
import ktx.ashley.oneOf
import ktx.log.logger
import me.baldo3000.showdown.ecs.component.RemoveComponent
import me.baldo3000.showdown.ecs.component.event.*
import me.baldo3000.showdown.ecs.createPlayer
import me.baldo3000.showdown.ecs.spawnWallsFromWorld
import me.baldo3000.showdown.world.ShowdownWorld

class GameEventsSystem(private val world: ShowdownWorld) : IteratingSystem(
    oneOf(
        DefeatComponent::class,
        VictoryComponent::class,
        PlayerSpawnComponent::class,
        WallsSpawnComponent::class
    ).exclude(RemoveComponent::class).get()
) {
    override fun setProcessing(processing: Boolean) {
        super.setProcessing(processing)
        if (!processing) world.reset()
    }

    override fun processEntity(entity: Entity, deltaTime: Float) {
        val event: EventComponent = entity[DefeatComponent.mapper]
            ?: entity[VictoryComponent.mapper]
            ?: entity[PlayerSpawnComponent.mapper]
            ?: entity[WallsSpawnComponent.mapper]
            ?: throw IllegalArgumentException("Entity must be an event")

        processEvent(event)
        entity.add(RemoveComponent())
    }

    private fun processEvent(event: EventComponent) {
        when (event) {
            is DefeatComponent -> log.info { "Game lost!" }
            is VictoryComponent -> log.info { "Game won!" }
            is PlayerSpawnComponent -> processPlayerSpawnEvent(event)
            is WallsSpawnComponent -> spawnWalls()
        }
    }

    private fun processPlayerSpawnEvent(event: PlayerSpawnComponent) {
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
