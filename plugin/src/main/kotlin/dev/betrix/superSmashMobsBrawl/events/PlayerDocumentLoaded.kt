package dev.betrix.superSmashMobsBrawl.events

import dev.betrix.superSmashMobsBrawl.models.player.PlayerDocument
import gg.flyte.twilight.event.TwilightEvent
import org.bukkit.entity.Player

class PlayerDocumentLoaded(val player: Player, val document: PlayerDocument) : TwilightEvent()