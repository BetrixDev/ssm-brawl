package dev.betrix.supersmashmobsbrawl.disguises

import dev.betrix.supersmashmobsbrawl.SSMBPlayer
import me.libraryaddict.disguise.DisguiseAPI
import me.libraryaddict.disguise.disguisetypes.MobDisguise

abstract class SSMBDisguise(private val player: SSMBPlayer) {
    lateinit var disguise: MobDisguise

    open fun createDisguise() {
        DisguiseAPI.disguiseToAll(player.bukkitPlayer, disguise)
        disguise.startDisguise()
    }

    open fun destroyDisguise() {
        disguise.removeDisguise()
    }
}