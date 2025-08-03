package dev.betrix.superSmashMobsBrawl.disguises

import dev.betrix.superSmashMobsBrawl.models.Hitbox
import me.libraryaddict.disguise.disguisetypes.DisguiseType
import me.libraryaddict.disguise.disguisetypes.MobDisguise
import org.bukkit.entity.Player

class SkeletonDisguise(player: Player) : Disguise(player) {
    override fun setup() {
        disguise = MobDisguise(DisguiseType.SKELETON)
        hitbox = Hitbox(0.6, 1.99)

        super.setup()
    }
}
