package dev.betrix.superSmashMobsBrawl.disguises

import dev.betrix.superSmashMobsBrawl.models.Hitbox
import me.libraryaddict.disguise.disguisetypes.DisguiseType
import me.libraryaddict.disguise.disguisetypes.MobDisguise
import me.libraryaddict.disguise.disguisetypes.watchers.WolfWatcher
import org.bukkit.DyeColor
import org.bukkit.entity.Player

class WolfDisguise(player: Player) : BrawlDisguise(player) {
    override val disguise = MobDisguise(DisguiseType.WOLF)
    override val hitbox = Hitbox(0.6, 0.85)

    fun setAngry(angry: Boolean) {
        (disguise.watcher as? WolfWatcher)?.isAngry = angry
    }

    fun setCollarColor(color: DyeColor) {
        (disguise.watcher as? WolfWatcher)?.collarColor = color
    }
}
