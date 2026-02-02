package me.baldo3000.showdown.ecs.component

import com.badlogic.ashley.core.Component
import com.badlogic.gdx.utils.Pool
import ktx.ashley.mapperFor

private const val MAX_HEALTH = 100f

class HealthComponent : Component, Pool.Poolable {
    var health = MAX_HEALTH

    override fun reset() {
        health = MAX_HEALTH
    }

    companion object {
        val mapper = mapperFor<HealthComponent>()
    }
}
