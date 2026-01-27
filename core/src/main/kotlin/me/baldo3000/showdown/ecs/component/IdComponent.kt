package me.baldo3000.showdown.ecs.component

import com.badlogic.ashley.core.Component
import com.badlogic.gdx.utils.Pool
import ktx.ashley.mapperFor
import kotlin.uuid.Uuid

class IdComponent : Component, Pool.Poolable {
    var id = Uuid.random()

    override fun reset() {
        id = Uuid.random()
    }

    companion object {
        val mapper = mapperFor<IdComponent>()
    }
}
