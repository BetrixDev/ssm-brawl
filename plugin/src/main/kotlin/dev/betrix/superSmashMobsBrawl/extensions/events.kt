package dev.betrix.superSmashMobsBrawl.extensions

import gg.flyte.twilight.event.TwilightListener
import gg.flyte.twilight.event.event
import org.bukkit.entity.Player

inline fun <reified T : org.bukkit.event.player.PlayerEvent> event(
    player: Player,
    noinline callback: T.() -> Unit,
): TwilightListener =
    event<T> twilightEvent@{
        if (this.player != player) return@twilightEvent
        callback()
    }
