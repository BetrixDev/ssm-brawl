package dev.betrix.superSmashMobsBrawl.hotbar

import org.bukkit.entity.Player

interface HotbarPreset {
    fun apply(player: Player): List<HotbarItem>
}

object HubHotbarPreset : HotbarPreset {
    override fun apply(player: Player): List<HotbarItem> {
        return listOf(HubQueueItem(player))
    }
}

