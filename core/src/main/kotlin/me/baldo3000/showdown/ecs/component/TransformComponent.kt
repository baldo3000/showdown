package me.baldo3000.showdown.ecs.component

import com.badlogic.ashley.core.Component
import com.badlogic.gdx.utils.Pool
import ktx.ashley.mapperFor
import me.baldo3000.showdown.data.Vector2D
import me.baldo3000.showdown.data.Vector3D

class TransformComponent : Component, Pool.Poolable, Comparable<TransformComponent> {
    val position = Vector3D()
    val previousPosition = Vector3D()
    val interpolatedPosition = Vector3D()
    val size = Vector2D(1f, 1f)
    var rotationDeg = 0f

    override fun reset() {
        setInitialPosition(0f, 0f, 0f)
        size.set(1f, 1f)
        rotationDeg = 0f
    }

    fun setInitialPosition(x: Float, y: Float, z: Float) {
        position.set(x, y, z)
        previousPosition.set(x, y, z)
        interpolatedPosition.set(x, y, z)
    }

    override fun compareTo(other: TransformComponent): Int {
        val zDiff = position.z.compareTo(other.position.z)
        return if (zDiff == 0) position.y.compareTo(other.position.y) else zDiff
    }

    companion object {
        val mapper = mapperFor<TransformComponent>()
    }
}
