package dev.betrix.superSmashMobsBrawl.components

import com.github.quillraven.fleks.Component
import com.github.quillraven.fleks.ComponentType
import dev.betrix.superSmashMobsBrawl.enums.Minigame
import org.bukkit.World
import org.bukkit.entity.Player

class MinigameComponent(val minigameData: Minigame, val players: List<Player>, val world: World) : Component<MinigameComponent> {
    override fun type() = MinigameComponent

    companion object : ComponentType<MinigameComponent>()
}