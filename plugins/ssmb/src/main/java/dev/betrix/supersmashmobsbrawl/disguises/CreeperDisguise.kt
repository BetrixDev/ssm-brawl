package dev.betrix.supersmashmobsbrawl.disguises

import dev.betrix.supersmashmobsbrawl.SSMBPlayer
import me.libraryaddict.disguise.disguisetypes.DisguiseType
import me.libraryaddict.disguise.disguisetypes.MobDisguise
import me.libraryaddict.disguise.disguisetypes.watchers.CreeperWatcher

class CreeperDisguise constructor(ssmbPlayer: SSMBPlayer) : SSMBDisguise(ssmbPlayer) {
    private val player = ssmbPlayer.bukkitPlayer

    init {
        disguise = MobDisguise(DisguiseType.CREEPER)
    }

    fun setPowered(isPowered: Boolean) {
        (disguise.watcher as CreeperWatcher).isPowered = isPowered
    }
}