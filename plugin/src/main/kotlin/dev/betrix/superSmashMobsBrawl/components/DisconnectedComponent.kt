package dev.betrix.superSmashMobsBrawl.components

import com.github.quillraven.fleks.Component
import com.github.quillraven.fleks.ComponentType

/** Marks a player who disconnected while in a minigame. */
class DisconnectedComponent : Component<DisconnectedComponent> {
    override fun type() = DisconnectedComponent

    companion object : ComponentType<DisconnectedComponent>()
}
