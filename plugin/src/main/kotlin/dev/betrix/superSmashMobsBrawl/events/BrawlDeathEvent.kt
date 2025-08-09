package dev.betrix.superSmashMobsBrawl.events

import dev.betrix.superSmashMobsBrawl.minigames.BrawlMinigame
import gg.flyte.twilight.event.TwilightEvent
import gg.flyte.twilight.event.TwilightListener
import gg.flyte.twilight.event.event
import org.bukkit.entity.Player

sealed class DeathReason {
    data object Void : DeathReason()
}

class BrawlDeathEvent(val player: Player, val reason: DeathReason) : TwilightEvent() {
    companion object {
        fun call(player: Player, reason: DeathReason): BrawlDeathEvent {
            val event = BrawlDeathEvent(player, reason)
            event.callEvent()
            return event
        }

        fun listen(
            minigame: BrawlMinigame<*>,
            callback: BrawlDeathEvent.() -> Unit,
        ): TwilightListener {
            return event<BrawlDeathEvent> {
                if (!minigame.hasPlayer(player)) {
                    return@event
                }

                callback()
            }
        }
    }
}
