package dev.betrix.superSmashMobsBrawl.disguises

import dev.betrix.superSmashMobsBrawl.models.Hitbox
import me.libraryaddict.disguise.disguisetypes.DisguiseType
import me.libraryaddict.disguise.disguisetypes.MobDisguise
import me.libraryaddict.disguise.disguisetypes.watchers.GuardianWatcher
import org.bukkit.entity.Entity
import org.bukkit.entity.Player

class GuardianDisguise(player: Player) : BrawlDisguise(player) {
    override val disguise = MobDisguise(DisguiseType.GUARDIAN)
    override val hitbox = Hitbox(0.85, 0.85)

    fun setTarget(target: Entity?) {
        (disguise.watcher as? GuardianWatcher)?.setTarget(target)
    }
}
