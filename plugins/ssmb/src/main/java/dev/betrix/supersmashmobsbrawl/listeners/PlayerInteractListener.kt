package dev.betrix.supersmashmobsbrawl.listeners

import dev.betrix.supersmashmobsbrawl.SSMBPlayer
import dev.betrix.supersmashmobsbrawl.abilities.tryUseSulphurBomb
import dev.betrix.supersmashmobsbrawl.enums.SSMBAbility
import dev.betrix.supersmashmobsbrawl.enums.TaggedKeyStr
import dev.betrix.supersmashmobsbrawl.extensions.get
import dev.betrix.supersmashmobsbrawl.extensions.has
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.block.Action
import org.bukkit.event.player.PlayerInteractEvent

class PlayerInteractListener : Listener {

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
            if (!event.item!!.itemMeta.persistentDataContainer.has(TaggedKeyStr.ABILITY_ITEM_ID)) {
                return
            }

            when (event.item!!.itemMeta.persistentDataContainer.get(TaggedKeyStr.ABILITY_ITEM_ID)) {
                SSMBAbility.SULPHUR_BOMB.id -> tryUseSulphurBomb(ssmbPlayer)
            }
        }
    }
}