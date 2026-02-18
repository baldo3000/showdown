package me.baldo3000.showdown.ecs.component.event

import ktx.ashley.mapperFor

class CheckGameEndComponent : EventComponent {
    override fun reset() {}

    companion object {
        val mapper = mapperFor<CheckGameEndComponent>()
    }
}
