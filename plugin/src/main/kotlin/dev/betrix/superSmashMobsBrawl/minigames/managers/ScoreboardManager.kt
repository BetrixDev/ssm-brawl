package dev.betrix.superSmashMobsBrawl.minigames.managers

import dev.betrix.superSmashMobsBrawl.IManageable
import dev.betrix.superSmashMobsBrawl.Manageable
import org.bukkit.OfflinePlayer

interface IScoreboardManager : IManageable {
    fun updateForPlayer(player: OfflinePlayer)
}

class DefaultScoreboardManager : Manageable(), IScoreboardManager {
    override fun updateForPlayer(player: OfflinePlayer) {}
}
