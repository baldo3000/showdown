package me.baldo3000.showdown.ecs.system

import com.badlogic.ashley.systems.IntervalSystem
import ktx.log.logger
import me.baldo3000.showdown.event.GameEventHandler
import me.baldo3000.showdown.event.api.GameEvent
import me.baldo3000.showdown.event.api.GameEventListener

private const val UPDATE_RATE = 1 / 30f

class EventSystem(private val eventHandler: GameEventHandler) : IntervalSystem(UPDATE_RATE), GameEventListener {
    override fun updateInterval() {
        eventHandler.processEvents()
    }

    override fun onEvent(event: GameEvent) {

    }

    companion object {
        private val log = logger<EventSystem>()
    }
}
