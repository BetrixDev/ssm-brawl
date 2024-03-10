package net.ssmb.abilities

import net.ssmb.SSMB
import org.bukkit.entity.Player
import org.bukkit.event.HandlerList
import org.bukkit.event.Listener

open class SSMBAbility(
    private val player: Player,
    private val plugin: SSMB,
    private val cooldown: Long,
    private val meta: Map<String, String>?
) : Listener {
    open fun tryActivateAbility() {}
    open fun initializeAbility() {
        plugin.server.pluginManager.registerEvents(this, plugin)
    }

    open fun destroyAbility() {
        HandlerList.unregisterAll(this)
    }
}