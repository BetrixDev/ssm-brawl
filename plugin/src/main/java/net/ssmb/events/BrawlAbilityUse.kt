package net.ssmb.events

import org.bukkit.entity.Player

class BrawlAbilityUse(val player: Player, val abilityId: String, val damageDealt: Double?) : BrawlEvent()