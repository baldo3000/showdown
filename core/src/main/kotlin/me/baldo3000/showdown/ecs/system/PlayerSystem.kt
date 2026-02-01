package me.baldo3000.showdown.ecs.system

import com.badlogic.ashley.core.Engine
import com.badlogic.ashley.core.EntitySystem
import me.baldo3000.showdown.event.GameEventHandler
import me.baldo3000.showdown.event.api.GameEvent
import me.baldo3000.showdown.event.api.GameEventListener

class PlayerSystem(private val eventHandler: GameEventHandler) : EntitySystem(), GameEventListener {
    override fun addedToEngine(engine: Engine?) {
        super.addedToEngine(engine)
        eventHandler.addListener(GameEvent.PlayerJoin::class, this)
    }

    override fun removedFromEngine(engine: Engine?) {
        super.removedFromEngine(engine)
        eventHandler.removeListener(GameEvent.PlayerJoin::class, this)
    }

    override fun onEvent(event: GameEvent) {
        if (event is GameEvent.PlayerJoin) {

        }
    }
}
