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

    companion object {
        fun getAbilityFromId(
            abilityId: String,
            player: Player,
            plugin: SSMB,
            cooldown: Long,
            meta: Map<String, String>?,
            index: Int
        ): SSMBAbility {
            return when (abilityId) {
                "sulphur_bomb" -> SulphurBombAbility(player, plugin, cooldown, meta, index)
                "explode" -> ExplodeAbility(player, plugin, cooldown, meta, index)
                else -> throw RuntimeException("No ability exists for id $abilityId")
            }
        }
    }

    open fun tryActivateAbility() {}
    open fun initializeAbility() {
        plugin.server.pluginManager.registerEvents(this, plugin)
    }

    open fun destroyAbility() {
        HandlerList.unregisterAll(this)
    }
}