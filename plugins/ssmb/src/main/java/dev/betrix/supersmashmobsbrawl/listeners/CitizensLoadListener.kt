package dev.betrix.supersmashmobsbrawl.listeners

import dev.betrix.supersmashmobsbrawl.SuperSmashMobsBrawl
import net.citizensnpcs.api.event.CitizensEnableEvent
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener

class CitizensLoadListener : Listener {
    private val plugin = SuperSmashMobsBrawl.instance

    @EventHandler
    fun onCitizensEnable(event: CitizensEnableEvent) {
    }
}