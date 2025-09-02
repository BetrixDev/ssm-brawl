package dev.betrix.superSmashMobsBrawl.components

import com.github.quillraven.fleks.Component
import com.github.quillraven.fleks.ComponentType

/** Marks an entity as eliminated from the current minigame (no further respawns). */
class EliminatedComponent : Component<EliminatedComponent> {
    override fun type() = EliminatedComponent

    companion object : ComponentType<EliminatedComponent>()
}