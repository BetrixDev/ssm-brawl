package dev.betrix.superSmashMobsBrawl.events

import dev.betrix.superSmashMobsBrawl.models.brawlData.KitDef
import gg.flyte.twilight.event.TwilightEvent
import org.bukkit.entity.Player

class PlayerSelectKitEvent(
    val player: Player,
    val kit: KitDef,
    var shouldSwitchImmediately: Boolean = false,
) : TwilightEvent() {

    companion object {
        fun call(
            player: Player,
            kit: KitDef,
            shouldSwitchImmediately: Boolean = false,
        ): PlayerSelectKitEvent {
            val event = PlayerSelectKitEvent(player, kit, shouldSwitchImmediately)
            event.callEvent()
            return event
        }
    }
}
