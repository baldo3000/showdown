package me.baldo3000.showdown.ecs.component

import com.badlogic.ashley.core.Component
import com.badlogic.gdx.utils.Pool
import ktx.ashley.mapperFor
import kotlin.uuid.Uuid

const val DEFAULT_DAMAGE = 5f

class DamageComponent : Component, Pool.Poolable {
    var damage = DEFAULT_DAMAGE
    var sourceId: Uuid? = null

    override fun reset() {
        damage = DEFAULT_DAMAGE
        sourceId = null
    }

    companion object {
        val mapper = mapperFor<DamageComponent>()
    }
}
