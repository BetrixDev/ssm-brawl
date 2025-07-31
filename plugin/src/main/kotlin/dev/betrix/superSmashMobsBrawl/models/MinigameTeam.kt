package dev.betrix.superSmashMobsBrawl.models

import org.bukkit.entity.Player

data class MinigameTeam(val players: MutableList<Player>, var stocks: Int)
