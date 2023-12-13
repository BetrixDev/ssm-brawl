package dev.betrix.supersmashmobsbrawl.kits

import org.bukkit.entity.Player
import org.bukkit.event.Listener

interface BaseKit : Listener {
    companion object {
        fun fromId(id: String, player: Player): BaseKit {
            return when (id) {
                "creeper" -> CreeperKit(player)
                else -> throw RuntimeException()
            }
        }
    }

    fun equipKit() {}
    fun removeKit() {}
}