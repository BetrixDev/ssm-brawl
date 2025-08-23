package dev.betrix.superSmashMobsBrawl.models

import org.bukkit.entity.Player

data class MinigamePlayer(
    val player: Player,
    val stats: MutableMap<String, Number> = mutableMapOf(),
    val teamId: String? = null,
)
