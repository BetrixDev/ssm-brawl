package dev.betrix.superSmashMobsBrawl.minigames.managers

import dev.betrix.superSmashMobsBrawl.Manageable
import org.bukkit.OfflinePlayer

interface IPlayerConnectionManager {
    fun markDisconnected(player: OfflinePlayer)

    fun markReconnected(player: OfflinePlayer)

    fun isDisconnected(player: OfflinePlayer): Boolean
}

class DefaultPlayerConnectionManager : Manageable(), IPlayerConnectionManager {
    private val disconnected = mutableSetOf<java.util.UUID>()

    override fun markDisconnected(player: OfflinePlayer) {
        disconnected.add(player.uniqueId)
    }

    override fun markReconnected(player: OfflinePlayer) {
        disconnected.remove(player.uniqueId)
    }

    override fun isDisconnected(player: OfflinePlayer): Boolean =
        disconnected.contains(player.uniqueId)
}
