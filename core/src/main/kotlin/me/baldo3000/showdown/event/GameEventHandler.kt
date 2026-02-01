package me.baldo3000.showdown.event

import com.badlogic.gdx.utils.ObjectMap
import ktx.collections.GdxSet
import ktx.log.logger
import me.baldo3000.showdown.event.api.GameEvent
import me.baldo3000.showdown.event.api.GameEventListener
import java.util.*
import kotlin.reflect.KClass

class GameEventHandler {
    private val listeners = ObjectMap<KClass<out GameEvent>, GdxSet<GameEventListener>>()
    private val events = LinkedList<GameEvent>()

    fun addListener(type: KClass<out GameEvent>, listener: GameEventListener) {
        var eventListeners = listeners[type]
        if (eventListeners == null) {
            eventListeners = GdxSet()
            listeners.put(type, eventListeners)
        }
        if (eventListeners.add(listener)) {
            LOG.debug { "Adding listener of type $type: $listener" }
        } else {
            LOG.error { "Trying to add already existing listener of type $type: $listener" }
        }
    }

    fun removeListener(type: KClass<out GameEvent>, listener: GameEventListener) {
        val eventListeners = listeners[type]
        when {
            eventListeners == null -> {
                LOG.error { "Trying to remove listener $listener from non-existing listeners of type $type" }
            }

            listener !in eventListeners -> {
                LOG.error { "Trying to remove non-existing listener of type $type: $listener" }
            }

            else -> {
                LOG.debug { "Removing listener of type $type: $listener" }
                eventListeners.remove(listener)
            }
        }
    }

    /**
     * This function removes the [listener] from all [types][GameEvent]. It is
     * slightly more efficient to use [removeListener] if you know the exact type(s).
     */
    fun removeListener(listener: GameEventListener) {
        LOG.debug { "Removing $listener from all types" }
        listeners.values().forEach { it.remove(listener) }
    }

    fun handleEvent(event: GameEvent) {
        events.addFirst(event)
    }

    fun processEvents() {
        while (events.isNotEmpty()) {
            val event: GameEvent = events.pop()
            listeners[event::class]?.forEach { it.onEvent(event) }
        }
    }

    fun nextEvent(): GameEvent? {
        return if (events.isNotEmpty()) events.pop() else null
    }

    companion object {
        private val LOG = logger<GameEventHandler>()
    }
}
