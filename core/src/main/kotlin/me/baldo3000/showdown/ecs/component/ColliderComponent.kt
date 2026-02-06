package me.baldo3000.showdown.ecs.component

import com.badlogic.ashley.core.Component
import com.badlogic.gdx.math.Circle
import com.badlogic.gdx.math.Shape2D
import com.badlogic.gdx.utils.Pool
import ktx.ashley.mapperFor

class ColliderComponent : Component, Pool.Poolable {
    var collider: Shape2D = Circle()

    override fun reset() {
        collider = Circle()
    }

    companion object {
        val mapper = mapperFor<ColliderComponent>()
    }
}
