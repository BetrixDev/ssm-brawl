@file:Suppress("DEPRECATION")

package dev.betrix.superSmashMobsBrawl.disguises

import dev.betrix.superSmashMobsBrawl.models.Hitbox
import io.github.retrooper.packetevents.util.SpigotConversionUtil
import me.libraryaddict.disguise.disguisetypes.DisguiseType
import me.libraryaddict.disguise.disguisetypes.MobDisguise
import me.libraryaddict.disguise.disguisetypes.watchers.EndermanWatcher
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.material.MaterialData

class EndermanDisguise(player: Player) : BrawlDisguise(player) {
    override val disguise = MobDisguise(DisguiseType.ENDERMAN)
    override val hitbox = Hitbox(0.6, 2.9)

    fun setHeldBlock(material: Material?) {
        (disguise.watcher as? EndermanWatcher)?.heldBlock =
            SpigotConversionUtil.fromBukkitMaterialData(MaterialData(material))
    }
}
