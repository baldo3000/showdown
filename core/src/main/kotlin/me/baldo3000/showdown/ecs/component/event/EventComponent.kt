package me.baldo3000.showdown.ecs.component.event

import com.badlogic.ashley.core.Component
import com.badlogic.gdx.utils.Pool

sealed interface EventComponent : Component, Pool.Poolable
