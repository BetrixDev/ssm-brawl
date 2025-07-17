package dev.betrix.superSmashMobsBrawl.components

import com.github.quillraven.fleks.Component
import com.github.quillraven.fleks.ComponentType
import org.bukkit.entity.Player

class MinigamePreflightComponent(val minigameId: String, var players: List<Player>) : Component<MinigamePreflightComponent> {
    override fun type() = MinigamePreflightComponent

    companion object : ComponentType<MinigamePreflightComponent>()
}