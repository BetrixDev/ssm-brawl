package dev.betrix.supersmashmobsbrawl.kits

import org.bukkit.entity.Player

interface BaseKit {
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