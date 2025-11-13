package dev.betrix.superSmashMobsBrawl.disguises

import dev.betrix.superSmashMobsBrawl.models.Hitbox
import me.libraryaddict.disguise.disguisetypes.DisguiseType
import me.libraryaddict.disguise.disguisetypes.MobDisguise
import me.libraryaddict.disguise.disguisetypes.watchers.SheepWatcher
import org.bukkit.DyeColor
import org.bukkit.entity.Player

class SheepDisguise(player: Player) : BrawlDisguise(player) {
    override val disguise = MobDisguise(DisguiseType.SHEEP)
    override val hitbox = Hitbox(0.9, 1.3)

    fun setColor(color: DyeColor) {
        (disguise.watcher as? SheepWatcher)?.color = color
    }

    fun setSheared(sheared: Boolean) {
        (disguise.watcher as? SheepWatcher)?.isSheared = sheared
    }

    fun isSheared(): Boolean {
        return (disguise.watcher as? SheepWatcher)?.isSheared ?: false
    }
}
