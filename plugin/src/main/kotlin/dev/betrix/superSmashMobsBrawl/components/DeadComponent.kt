package dev.betrix.superSmashMobsBrawl.components

import com.github.quillraven.fleks.Component
import com.github.quillraven.fleks.ComponentType
import dev.betrix.superSmashMobsBrawl.events.DeathReason

/** Marks an entity as dead within a minigame context and captures the reason. */
data class DeadComponent(
    val reason: DeathReason,
    val timeOfDeathMillis: Long = System.currentTimeMillis(),
) : Component<DeadComponent> {
    override fun type() = DeadComponent

    companion object : ComponentType<DeadComponent>()
}
