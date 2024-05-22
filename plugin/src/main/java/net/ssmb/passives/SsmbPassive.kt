package net.ssmb.passives

import net.ssmb.SSMB
import net.ssmb.dtos.minigame.MinigameStartSuccess
import org.bukkit.entity.Player
import org.bukkit.event.HandlerList
import org.bukkit.event.Listener

open class SsmbPassive(
    private val player: Player,
    private val passiveData: MinigameStartSuccess.PlayerData.KitData.PassiveEntry.PassiveData
): Listener {
    private val plugin = SSMB.instance

    open fun initializePassive() {
        plugin.server.pluginManager.registerEvents(this, plugin)
    }

    open fun destroyPassive() {
        HandlerList.unregisterAll(this)
    }

    fun getMetaDouble(key: String): Double? {
        return passiveData.meta?.get(key)?.toDouble()
    }

    fun getMetaDouble(key: String, default: Double): Double {
        return getMetaDouble(key) ?: default
    }

    fun getMetaInt(key: String): Int? {
        return passiveData.meta?.get(key)?.toInt()
    }

    fun getMetaInt(key: String, default: Int): Int {
        return getMetaInt(key) ?: default
    }
}