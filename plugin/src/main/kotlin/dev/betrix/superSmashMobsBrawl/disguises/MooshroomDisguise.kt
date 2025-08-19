package dev.betrix.superSmashMobsBrawl.disguises

import dev.betrix.superSmashMobsBrawl.models.Hitbox
import me.libraryaddict.disguise.disguisetypes.DisguiseType
import me.libraryaddict.disguise.disguisetypes.MobDisguise
import org.bukkit.entity.Player

class MooshroomDisguise(player: Player) : BrawlDisguise(player) {
    override val disguise =
        MobDisguise(resolveDisguiseType("MOOSHROOM", "MUSHROOM_COW", fallback = DisguiseType.COW))
    override val hitbox = Hitbox(0.9, 1.4)
}
