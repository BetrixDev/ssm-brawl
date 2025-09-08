package dev.betrix.superSmashMobsBrawl.components

import com.github.quillraven.fleks.Component
import com.github.quillraven.fleks.ComponentType

/** Marks a player who is waiting to respawn in a minigame. */
data class RespawningComponent(var remainingTicks: Int) : Component<RespawningComponent> {
    override fun type() = RespawningComponent

    companion object : ComponentType<RespawningComponent>()
}
