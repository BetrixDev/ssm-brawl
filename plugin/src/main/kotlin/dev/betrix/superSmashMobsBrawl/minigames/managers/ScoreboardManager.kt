package dev.betrix.superSmashMobsBrawl.minigames.managers

import dev.betrix.superSmashMobsBrawl.Manageable
import org.bukkit.OfflinePlayer

interface IScoreboardManager {
    fun initialize()

    fun updateForPlayer(player: OfflinePlayer)

    fun teardown()
}

class DefaultScoreboardManager : Manageable(), IScoreboardManager {
    override fun initialize() {}

    override fun updateForPlayer(player: OfflinePlayer) {}

    override fun teardown() {
        super.teardown()
    }
}
