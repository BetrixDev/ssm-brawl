package dev.betrix.superSmashMobsBrawl.events

import gg.flyte.twilight.event.TwilightEvent

sealed class DeathReason {
    data object Void : DeathReason()

    data object Damage : DeathReason()
}

class BrawlDeathEvent(val player: org.bukkit.entity.Player, val reason: DeathReason) :
    TwilightEvent() {
    companion object {
        fun call(player: org.bukkit.entity.Player, reason: DeathReason): BrawlDeathEvent {
            val event = BrawlDeathEvent(player, reason)
            event.callEvent()
            return event
        }
    }
}
