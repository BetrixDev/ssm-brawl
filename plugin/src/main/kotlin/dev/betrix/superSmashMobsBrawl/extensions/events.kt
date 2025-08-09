package dev.betrix.superSmashMobsBrawl.extensions

import gg.flyte.twilight.event.TwilightListener
import gg.flyte.twilight.event.event
import org.bukkit.entity.Player
import org.bukkit.event.player.PlayerInteractEvent

fun <T : PlayerInteractEvent> event(
    player: Player,
    callback: PlayerInteractEvent.() -> Unit,
): TwilightListener {
    return event<PlayerInteractEvent> interactEvent@{
        if (this.player != player) {
            return@interactEvent
        }

        callback()
    }
}
