package dev.betrix.superSmashMobsBrawl.components

import com.github.quillraven.fleks.Component
import com.github.quillraven.fleks.ComponentType
import org.bukkit.entity.Player

data class MinecraftPlayerComponent(val player: Player) : Component<MinecraftPlayerComponent> {
    override fun type() = MinecraftPlayerComponent

    companion object : ComponentType<MinecraftPlayerComponent>()
}
