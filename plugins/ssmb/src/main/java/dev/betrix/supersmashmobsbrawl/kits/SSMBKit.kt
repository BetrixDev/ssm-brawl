package dev.betrix.supersmashmobsbrawl.kits

import dev.betrix.supersmashmobsbrawl.enums.TaggedKeyNum
import dev.betrix.supersmashmobsbrawl.extensions.setMetadata
import dev.betrix.supersmashmobsbrawl.managers.api.payloads.StartGameResponse
import org.bukkit.entity.Player
import org.bukkit.event.Listener

abstract class SSMBKit(private val player: Player, private val kitData: StartGameResponse.KitData) : Listener {

    init {
        player.setMetadata {
            set(TaggedKeyNum.PLAYER_KIT_KNOCKBACK_MULT, kitData.knockback)
            set(TaggedKeyNum.PLAYER_KIT_DAMAGE, kitData.damage)
            set(TaggedKeyNum.PLAYER_KIT_ARMOR, kitData.armor)
        }
    }

    open fun destroyKit() {}
}