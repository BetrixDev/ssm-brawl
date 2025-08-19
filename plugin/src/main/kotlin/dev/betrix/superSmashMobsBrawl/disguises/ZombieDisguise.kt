package dev.betrix.superSmashMobsBrawl.disguises

import dev.betrix.superSmashMobsBrawl.models.Hitbox
import me.libraryaddict.disguise.disguisetypes.DisguiseType
import me.libraryaddict.disguise.disguisetypes.MobDisguise
import me.libraryaddict.disguise.disguisetypes.watchers.ZombieWatcher
import org.bukkit.entity.Player

class ZombieDisguise(player: Player) : BrawlDisguise(player) {
    override val disguise = MobDisguise(DisguiseType.ZOMBIE)
    override val hitbox = Hitbox(0.6, 1.95)

    fun setBaby(isBaby: Boolean) {
        (disguise.watcher as? ZombieWatcher)?.isBaby = isBaby
    }
}
