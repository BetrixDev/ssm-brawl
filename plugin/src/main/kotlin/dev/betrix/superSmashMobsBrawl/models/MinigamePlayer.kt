package dev.betrix.superSmashMobsBrawl.models

import org.bukkit.entity.Player

data class MinigamePlayer(
    val player: Player,
    val stats: Map<String, Number> = emptyMap(),
    val stocks: Int? = null,
    val teamId: String? = null
) {
    val hasStocks: Boolean get() = stocks != null
    val hasTeam: Boolean get() = teamId != null

    fun withStocks(stocks: Int) = copy(stocks = stocks)
    fun withTeam(teamId: String) = copy(teamId = teamId)
}