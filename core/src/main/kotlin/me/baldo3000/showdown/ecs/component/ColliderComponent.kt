package me.baldo3000.showdown.ecs.component

import com.badlogic.ashley.core.Component
import com.badlogic.gdx.math.Circle
import com.badlogic.gdx.utils.Pool
import ktx.ashley.mapperFor

class ColliderComponent : Component, Pool.Poolable {
    val collider: Circle = Circle()

    override fun reset() {
        collider.set(0f, 0f, 0f)
    }

    companion object {
        val mapper = mapperFor<ColliderComponent>()
    }
}
