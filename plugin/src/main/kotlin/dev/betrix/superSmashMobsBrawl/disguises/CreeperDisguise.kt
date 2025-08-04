package dev.betrix.superSmashMobsBrawl.disguises

import dev.betrix.superSmashMobsBrawl.models.Hitbox
import me.libraryaddict.disguise.disguisetypes.DisguiseType
import me.libraryaddict.disguise.disguisetypes.MobDisguise
import me.libraryaddict.disguise.disguisetypes.watchers.CreeperWatcher
import org.bukkit.entity.Player

class CreeperDisguise(player: Player) : Disguise(player) {
    override val disguise = MobDisguise(DisguiseType.CREEPER)
    override val hitbox = Hitbox(0.6, 1.7)

    fun setIgnited(charged: Boolean) {
        (disguise.watcher as? CreeperWatcher)?.isIgnited = charged
    }

    fun setCharged(charged: Boolean) {
        (disguise.watcher as? CreeperWatcher)?.isPowered = charged
    }
}
