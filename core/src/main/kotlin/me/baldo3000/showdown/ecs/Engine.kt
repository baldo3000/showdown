package me.baldo3000.showdown.ecs

import com.badlogic.ashley.core.Engine
import com.badlogic.ashley.core.Entity
import ktx.ashley.allOf
import ktx.ashley.exclude
import me.baldo3000.showdown.ecs.component.*

private val playersFamily =
    allOf(
        IdComponent::class,
        HealthComponent::class,
        TransformComponent::class,
        MoveComponent::class,
        ColliderComponent::class,
        GraphicComponent::class
    ).exclude(RemoveComponent::class).get()

private val characterFamily =
    allOf(
        InputComponent::class
    ).exclude(RemoveComponent::class).get()

private val bulletsFamily =
    allOf(
        IdComponent::class,
        DamageComponent::class,
        TransformComponent::class,
        MoveComponent::class,
        ColliderComponent::class,
        GraphicComponent::class
    ).exclude(RemoveComponent::class).get()

private val wallsFamily =
    allOf(
        IdComponent::class,
        TransformComponent::class,
        ColliderComponent::class,
        GraphicComponent::class
    ).exclude(
        RemoveComponent::class,
        HealthComponent::class,
        MoveComponent::class
    ).get()

val Engine.players: List<Entity>
    get() = getEntitiesFor(playersFamily).toList()

val Engine.character: Entity?
    get() = getEntitiesFor(characterFamily).firstOrNull()

val Engine.bullets: List<Entity>
    get() = getEntitiesFor(bulletsFamily).toList()

val Engine.walls: List<Entity>
    get() = getEntitiesFor(wallsFamily).toList()
