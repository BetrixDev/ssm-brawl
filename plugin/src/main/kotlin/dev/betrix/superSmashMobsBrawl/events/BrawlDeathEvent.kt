package dev.betrix.superSmashMobsBrawl.events

import gg.flyte.twilight.event.TwilightEvent
import gg.flyte.twilight.event.TwilightListener
import gg.flyte.twilight.event.event
import org.bukkit.entity.Player

enum class HazardType {
    LIQUID,
    VOID
}

sealed class DeathReason {
    data class Hazard(val type: HazardType) : DeathReason()

    data object Damage : DeathReason()
}

class BrawlDeathEvent(val player: Player, val reason: DeathReason) : TwilightEvent() {
    companion object {
        fun call(player: Player, reason: DeathReason): BrawlDeathEvent {
            val event = BrawlDeathEvent(player, reason)
            event.callEvent()
            return event
        }

        fun listen(callback: BrawlDeathEvent.() -> Unit): TwilightListener =
            event<BrawlDeathEvent> { callback() }
    }
}
