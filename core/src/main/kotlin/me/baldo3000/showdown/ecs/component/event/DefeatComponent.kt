package me.baldo3000.showdown.ecs.component.event

import ktx.ashley.mapperFor

class DefeatComponent : EventComponent {
    override fun reset() {}

    companion object {
        val mapper = mapperFor<DefeatComponent>()
    }
}
