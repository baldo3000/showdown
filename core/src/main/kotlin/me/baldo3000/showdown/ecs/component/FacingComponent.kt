package me.baldo3000.showdown.ecs.component

import com.badlogic.ashley.core.Component
import com.badlogic.gdx.utils.Pool
import kotlinx.serialization.Serializable
import ktx.ashley.mapperFor

@Serializable
enum class FacingDirection { DEFAULT, TOP, TOP_RIGHT, RIGHT, BOTTOM_RIGHT, BOTTOM, BOTTOM_LEFT, LEFT, TOP_LEFT }

class FacingComponent : Component, Pool.Poolable {
    var direction = FacingDirection.DEFAULT

    override fun reset() {
        direction = FacingDirection.DEFAULT
    }

    companion object {
        val mapper = mapperFor<FacingComponent>()
    }
}
