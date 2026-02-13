package me.baldo3000.showdown.ecs.component.event

import ktx.ashley.mapperFor
import kotlin.uuid.Uuid

class PlayerJoinComponent : EventComponent {
    var playerId: Uuid = Uuid.random()
    var controllable: Boolean = false

    override fun reset() {
        playerId = Uuid.random()
        controllable = false
    }

    companion object {
        val mapper = mapperFor<PlayerJoinComponent>()
    }
}
