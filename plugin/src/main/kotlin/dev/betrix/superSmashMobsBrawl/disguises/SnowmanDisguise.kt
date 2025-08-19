package dev.betrix.superSmashMobsBrawl.disguises

import dev.betrix.superSmashMobsBrawl.models.Hitbox
import me.libraryaddict.disguise.disguisetypes.DisguiseType
import me.libraryaddict.disguise.disguisetypes.MobDisguise
import me.libraryaddict.disguise.disguisetypes.watchers.SnowmanWatcher
import org.bukkit.entity.Player

class SnowmanDisguise(player: Player) : BrawlDisguise(player) {
    override val disguise = MobDisguise(DisguiseType.SNOWMAN)
    override val hitbox = Hitbox(0.7, 1.9)

    fun setDerp(derp: Boolean) {
        (disguise.watcher as? SnowmanWatcher)?.isDerp = derp
    }
}
