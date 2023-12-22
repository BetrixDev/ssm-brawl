package dev.betrix.supersmashmobsbrawl.kits

import dev.betrix.supersmashmobsbrawl.enums.TaggedKeyNum
import dev.betrix.supersmashmobsbrawl.extensions.metadata
import dev.betrix.supersmashmobsbrawl.managers.api.payloads.StartGameResponse
import org.bukkit.entity.Player
import org.bukkit.event.Listener

abstract class SSMBKit(private val player: Player, private val kitData: StartGameResponse.KitData) : Listener {
    companion object {
        fun kitFromId(id: String, player: Player, kitData: StartGameResponse.KitData): SSMBKit {
            return when (id) {
                "creeper" -> CreeperKit(player, kitData)
                else -> throw RuntimeException("Unable to find kit with id \"$id\"")
            }
        }
    }

    open fun equipKit() {
        player.metadata {
            set(TaggedKeyNum.PLAYER_KIT_KNOCKBACK_MULT, kitData.knockback)
            set(TaggedKeyNum.PLAYER_KIT_DAMAGE, kitData.damage)
            set(TaggedKeyNum.PLAYER_KIT_ARMOR, kitData.armor)
        }
    }

    open fun destroyKit() {
        player.metadata {
            remove(TaggedKeyNum.PLAYER_KIT_KNOCKBACK_MULT)
            remove(TaggedKeyNum.PLAYER_KIT_DAMAGE)
            remove(TaggedKeyNum.PLAYER_KIT_ARMOR)
        }
    }
}