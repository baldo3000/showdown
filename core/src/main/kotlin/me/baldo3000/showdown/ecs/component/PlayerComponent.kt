package me.baldo3000.showdown.ecs.component

import com.badlogic.ashley.core.Component
import com.badlogic.gdx.utils.Pool
import ktx.ashley.mapperFor

private const val MAX_LIFE = 100f
private const val MAX_SHIELD = 100f

class PlayerComponent : Component, Pool.Poolable {
    var life = MAX_LIFE
    val maxLife = MAX_LIFE
    var shield = 0f
    val maxShield = MAX_SHIELD
    var score = 0f

    override fun reset() {
        life = MAX_LIFE
        shield = 0f
        score = 0f
    }

    companion object {
        val mapper = mapperFor<PlayerComponent>()
    }
}
