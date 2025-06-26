package dev.betrix.combatServer.components

import com.github.quillraven.fleks.Component
import com.github.quillraven.fleks.ComponentType
import org.bukkit.entity.Player

data class MinecraftPlayer(
    val player: Player
) : Component<MinecraftPlayer> {
    override fun type() = MinecraftPlayer

    companion object : ComponentType<MinecraftPlayer>()
}