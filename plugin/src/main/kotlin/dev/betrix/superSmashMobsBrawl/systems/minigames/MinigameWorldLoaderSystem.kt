package dev.betrix.superSmashMobsBrawl.systems.minigames

import com.github.quillraven.fleks.Entity
import com.github.quillraven.fleks.IteratingSystem
import com.github.quillraven.fleks.World.Companion.family
import com.github.quillraven.fleks.World.Companion.inject
import dev.betrix.superSmashMobsBrawl.SuperSmashMobsBrawl
import dev.betrix.superSmashMobsBrawl.components.MinigameComponent
import dev.betrix.superSmashMobsBrawl.components.MinigameState

class MinigameWorldLoaderSystem(private val plugin: SuperSmashMobsBrawl = inject()) : IteratingSystem(
    family { all(MinigameComponent) }
) {
    override fun onTickEntity(entity: Entity) {
        val minigame = entity[MinigameComponent]

        if (minigame.state != MinigameState.LOADING_WORLD) {
            return
        }
    }
}