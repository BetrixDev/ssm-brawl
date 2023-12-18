package dev.betrix.supersmashmobsbrawl.disguises

import me.libraryaddict.disguise.DisguiseAPI
import me.libraryaddict.disguise.disguisetypes.MobDisguise
import org.bukkit.entity.Player

abstract class SSMBDisguise(private val player: Player) {
    lateinit var disguise: MobDisguise

    open fun createDisguise() {
        DisguiseAPI.disguiseToAll(player, disguise)
        disguise.startDisguise()
    }

    open fun destroyDisguise() {
        disguise.removeDisguise()
    }
}