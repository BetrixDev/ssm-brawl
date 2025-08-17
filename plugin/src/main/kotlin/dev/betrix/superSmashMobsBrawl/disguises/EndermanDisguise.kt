package dev.betrix.superSmashMobsBrawl.disguises

import dev.betrix.superSmashMobsBrawl.models.Hitbox
import me.libraryaddict.disguise.disguisetypes.DisguiseType
import me.libraryaddict.disguise.disguisetypes.MobDisguise
import org.bukkit.Material
import org.bukkit.entity.Enderman
import org.bukkit.entity.Player

class EndermanDisguise(player: Player) : BrawlDisguise(player) {
    override val disguise = MobDisguise(DisguiseType.ENDERMAN)
    override val hitbox = Hitbox(2.9, 0.6)

    fun setHeldBlock(material: Material?) {
        (disguise.entity as? Enderman)?.carriedBlock = material?.createBlockData()
    }
}
