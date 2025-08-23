package dev.betrix.superSmashMobsBrawl.events

import gg.flyte.twilight.event.TwilightEvent
import org.bukkit.entity.Player

class QueuePopEvent(val minigameId: String, val players: List<Player>) : TwilightEvent()