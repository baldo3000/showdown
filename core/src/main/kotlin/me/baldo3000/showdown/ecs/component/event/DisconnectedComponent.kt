package me.baldo3000.showdown.ecs.component.event

import ktx.ashley.mapperFor

class DisconnectedComponent : EventComponent {
    override fun reset() {}

    companion object {
        val mapper = mapperFor<DisconnectedComponent>()
    }
}
