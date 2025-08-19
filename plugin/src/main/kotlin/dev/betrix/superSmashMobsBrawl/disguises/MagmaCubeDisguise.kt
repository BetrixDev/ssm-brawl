package dev.betrix.superSmashMobsBrawl.disguises

import dev.betrix.superSmashMobsBrawl.models.Hitbox
import me.libraryaddict.disguise.disguisetypes.DisguiseType
import me.libraryaddict.disguise.disguisetypes.MobDisguise
import me.libraryaddict.disguise.disguisetypes.watchers.SlimeWatcher
import org.bukkit.entity.Player

class MagmaCubeDisguise(player: Player) : BrawlDisguise(player) {
    override val disguise = MobDisguise(DisguiseType.MAGMA_CUBE)
    override val hitbox = Hitbox(0.6, 0.6)

    fun setSize(size: Int) {
        (disguise.watcher as? SlimeWatcher)?.size = size
    }
}
