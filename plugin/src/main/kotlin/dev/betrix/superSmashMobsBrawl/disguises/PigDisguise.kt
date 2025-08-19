package dev.betrix.superSmashMobsBrawl.disguises

import dev.betrix.superSmashMobsBrawl.models.Hitbox
import me.libraryaddict.disguise.disguisetypes.DisguiseType
import me.libraryaddict.disguise.disguisetypes.MobDisguise
import me.libraryaddict.disguise.disguisetypes.watchers.PigWatcher
import org.bukkit.entity.Player

class PigDisguise(player: Player) : BrawlDisguise(player) {
    override val disguise = MobDisguise(DisguiseType.PIG)
    override val hitbox = Hitbox(0.9, 0.9)

    fun setSaddled(saddled: Boolean) {
        (disguise.watcher as? PigWatcher)?.isSaddled = saddled
    }
}
