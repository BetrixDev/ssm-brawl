package dev.betrix.supersmashmobsbrawl.listeners

import dev.betrix.supersmashmobsbrawl.SSMBPlayer
import dev.betrix.supersmashmobsbrawl.SuperSmashMobsBrawl
import dev.betrix.supersmashmobsbrawl.abilities.tryUseSulphurBomb
import dev.betrix.supersmashmobsbrawl.enums.SSMBAbility
import dev.betrix.supersmashmobsbrawl.enums.TaggedKey
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.block.Action
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.persistence.PersistentDataType

class PlayerInteractListener : Listener {

    private val plugin = SuperSmashMobsBrawl.instance

    @EventHandler
    fun onPlayerInteract(event: PlayerInteractEvent) {
        val ssmbPlayer = SSMBPlayer.fromUuid(event.player.uniqueId) ?: return

        if (event.item == null) {
            return
        }

        if (
            event.action == Action.RIGHT_CLICK_BLOCK ||
            event.action == Action.RIGHT_CLICK_AIR
        ) {
            if (!event.item!!.itemMeta.persistentDataContainer.has(TaggedKey.ABILITY_ITEM_ID.key)) {
                return
            }

            val taggedItemId =
                event.item!!.itemMeta.persistentDataContainer.get(
                    TaggedKey.ABILITY_ITEM_ID.key,
                    PersistentDataType.STRING
                ) ?: return

            if (taggedItemId == SSMBAbility.SULPHUR_BOMB.id) {
                tryUseSulphurBomb(ssmbPlayer)
            }
        }
    }
}