package me.baldo3000.showdown.ecs

import com.badlogic.ashley.core.Engine
import com.badlogic.ashley.core.Entity
import ktx.ashley.get
import me.baldo3000.showdown.ecs.component.*

val Engine.players: List<Entity>
    get() = entities.filter { entity ->
        entity[IdComponent.mapper] != null &&
            entity[PlayerComponent.mapper] != null &&
            entity[TransformComponent.mapper] != null &&
            entity[MoveComponent.mapper] != null &&
            entity[RemoveComponent.mapper] == null
    }
