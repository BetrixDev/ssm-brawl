package dev.betrix.superSmashMobsBrawl.minigames.models

import org.bukkit.entity.Player

data class MinigamePlayer(
    val player: Player,
    val stats: MutableMap<String, Number> = mutableMapOf(),
    val stocks: Int? = null,
    val teamId: String? = null,
    val status: MinigamePlayerStatus = MinigamePlayerStatus.ALIVE,
) {
    val hasStocks: Boolean get() = stocks != null
    val hasTeam: Boolean get() = teamId != null
    val isAlive: Boolean get() = status == MinigamePlayerStatus.ALIVE
    val isDead: Boolean get() = status == MinigamePlayerStatus.DEAD
    val isSpectating: Boolean get() = status == MinigamePlayerStatus.SPECTATING
    val isRespawning: Boolean get() = status == MinigamePlayerStatus.RESPAWNING

    fun withStocks(stocks: Int) = copy(stocks = stocks)
    fun withTeam(teamId: String) = copy(teamId = teamId)

    fun updateStat(key: String, value: Number): MinigamePlayer {
        val newStats = stats.toMutableMap()
        newStats[key] = value
        return copy(stats = newStats)
    }

    fun incrementStat(key: String, amount: Number = 1): MinigamePlayer {
        val currentValue = stats[key]?.toDouble() ?: 0.0
        val newValue = currentValue + amount.toDouble()
        return updateStat(key, newValue)
    }

    fun addKill() = incrementStat("kills")
    fun addDeath() = incrementStat("deaths")
    fun addAssist() = incrementStat("assists")
    fun addDamageDealt(damage: Double) = incrementStat("damageDealt", damage)
    fun addDamageTaken(damage: Double) = incrementStat("damageTaken", damage)
}

