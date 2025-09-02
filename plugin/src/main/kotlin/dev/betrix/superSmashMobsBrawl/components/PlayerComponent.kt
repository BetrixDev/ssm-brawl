package dev.betrix.superSmashMobsBrawl.components

import com.github.quillraven.fleks.Component
import com.github.quillraven.fleks.ComponentType
import org.bukkit.entity.Player

data class PlayerComponent(val player: Player) : Component<PlayerComponent> {
    override fun type() = PlayerComponent

    companion object : ComponentType<PlayerComponent>()
}
