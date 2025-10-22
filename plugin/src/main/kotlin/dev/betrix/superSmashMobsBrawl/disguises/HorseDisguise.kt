package dev.betrix.superSmashMobsBrawl.disguises

import dev.betrix.superSmashMobsBrawl.models.Hitbox
import me.libraryaddict.disguise.disguisetypes.DisguiseType
import me.libraryaddict.disguise.disguisetypes.MobDisguise
import me.libraryaddict.disguise.disguisetypes.watchers.HorseWatcher
import org.bukkit.entity.Horse
import org.bukkit.entity.Player

class HorseDisguise(player: Player) : BrawlDisguise(player) {
    override val disguise =
        MobDisguise(DisguiseType.HORSE).apply {
            (watcher as HorseWatcher).color = Horse.Color.CREAMY
        }
    override val hitbox = Hitbox(1.4, 1.6)
}
