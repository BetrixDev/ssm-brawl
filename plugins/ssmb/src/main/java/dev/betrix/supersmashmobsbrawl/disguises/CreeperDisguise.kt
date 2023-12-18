package dev.betrix.supersmashmobsbrawl.disguises

import me.libraryaddict.disguise.disguisetypes.DisguiseType
import me.libraryaddict.disguise.disguisetypes.MobDisguise
import me.libraryaddict.disguise.disguisetypes.watchers.CreeperWatcher
import org.bukkit.entity.Player

class CreeperDisguise constructor(player: Player) : SSMBDisguise(player) {

    init {
        disguise = MobDisguise(DisguiseType.CREEPER)
    }

    fun setPowered(isPowered: Boolean) {
        (disguise.watcher as CreeperWatcher).isPowered = isPowered
    }

    fun setIgnited(isIgnited: Boolean) {
        (disguise.watcher as CreeperWatcher).isIgnited = isIgnited
    }
}