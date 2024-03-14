package net.ssmb.passives

import org.bukkit.entity.Player

fun constructPassiveFromId(player: Player, passiveId: String, meta: Map<String, String>?): IPassive {
    return when (passiveId) {
        "hunger" -> HungerPassive(player, meta!!)
        "double_jump" -> DoubleJumpPassive(player)
        "lightning_shield" -> LightningShieldPassive(player)
        "regeneration" -> RegenerationPassive(player, meta!!)
        else -> throw RuntimeException("")
    }
}