package dev.betrix.superSmashMobsBrawl.disguises

import dev.betrix.superSmashMobsBrawl.models.Hitbox
import me.libraryaddict.disguise.disguisetypes.DisguiseType
import me.libraryaddict.disguise.disguisetypes.MobDisguise
import me.libraryaddict.disguise.disguisetypes.watchers.CreeperWatcher
import org.bukkit.entity.Player

class CreeperDisguise(player: Player) : Disguise(player) {
    override fun setup() {
        disguise = MobDisguise(DisguiseType.CREEPER)
        hitbox = Hitbox(0.6, 1.7)

        super.setup()
    }

    fun isIgnited(charged: Boolean) {
        (disguise.watcher as CreeperWatcher).isIgnited = charged
    }

    fun isCharged(charged: Boolean) {
        (disguise.watcher as CreeperWatcher).isPowered = charged
    }
}
