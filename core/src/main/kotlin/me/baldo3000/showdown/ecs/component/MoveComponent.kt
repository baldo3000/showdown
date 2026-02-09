package me.baldo3000.showdown.ecs.component

import com.badlogic.ashley.core.Component
import com.badlogic.gdx.utils.Pool
import ktx.ashley.mapperFor
import me.baldo3000.showdown.data.Vector2D

class MoveComponent : Component, Pool.Poolable {
    val speed = Vector2D()

    override fun reset() {
        speed.set(0f, 0f)
    }

    companion object {
        val mapper = mapperFor<MoveComponent>()
    }
}
