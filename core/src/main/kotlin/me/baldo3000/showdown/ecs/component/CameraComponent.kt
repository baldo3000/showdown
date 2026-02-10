package me.baldo3000.showdown.ecs.component

import com.badlogic.ashley.core.Component
import com.badlogic.gdx.utils.Pool
import ktx.ashley.mapperFor

class CameraComponent : Component, Pool.Poolable {
    override fun reset() {}

    companion object {
        val mapper = mapperFor<CameraComponent>()
    }
}
