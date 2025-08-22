package dev.betrix.superSmashMobsBrawl.events

import gg.flyte.twilight.event.TwilightEvent
import org.bukkit.entity.Player

class PlayerSelectKitEvent(val player: Player, val kitId: String) : TwilightEvent()