package me.baldo3000.showdown.ecs.system

import com.badlogic.ashley.core.Entity
import com.badlogic.ashley.systems.IteratingSystem
import ktx.ashley.exclude
import ktx.ashley.get
import ktx.ashley.oneOf
import ktx.log.logger
import me.baldo3000.showdown.ecs.character
import me.baldo3000.showdown.ecs.component.RemoveComponent
import me.baldo3000.showdown.ecs.component.event.CheckGameEndComponent
import me.baldo3000.showdown.ecs.component.event.EventComponent
import me.baldo3000.showdown.ecs.players
import me.baldo3000.showdown.game.EntityFactory
import me.baldo3000.showdown.game.GameState

class GameEventsSystem(
    private val entityFactory: EntityFactory,
    private val gameState: GameState
) : IteratingSystem(
    oneOf(
        CheckGameEndComponent::class
    ).exclude(RemoveComponent::class).get()
) {
    private var onGameEnd: (placement: Int) -> Unit = {}

    fun setOnGameEnd(onGameEnd: (placement: Int) -> Unit) {
        this.onGameEnd = onGameEnd
    }

    override fun setProcessing(processing: Boolean) {
        super.setProcessing(processing)
        if (!processing) gameState.reset()
    }

    override fun processEntity(entity: Entity, deltaTime: Float) {
        val event: EventComponent = entity[CheckGameEndComponent.mapper]
            ?: throw IllegalArgumentException("Entity must be an event")

        processEvent(event)
        entity.add(RemoveComponent())
    }

    private fun processEvent(event: EventComponent) {
        when (event) {
            is CheckGameEndComponent -> checkGameEnd()
        }
    }

    private fun checkGameEnd() {
        if (engine.character == null) {
            onGameEnd(engine.players.size + 1)
        } else if (engine.players.size == 1) {
            onGameEnd(1)
        }
    }

    companion object {
        private val log = logger<GameEventsSystem>()
    }
}
