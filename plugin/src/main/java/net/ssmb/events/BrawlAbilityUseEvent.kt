package net.ssmb.events

import org.bukkit.entity.Player

class BrawlAbilityUseEvent(val player: Player, val abilityId: String, val damageDealt: Double?) :
    BrawlEvent()
