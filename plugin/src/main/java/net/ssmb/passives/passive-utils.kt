package net.ssmb.passives

import net.ssmb.dtos.minigame.MinigameStartSuccess
import org.bukkit.entity.Player

fun constructPassiveFromData(
    player: Player,
    passiveEntry: MinigameStartSuccess.PlayerData.KitData.PassiveEntry
): IPassive {
    return when (passiveEntry.passive.id) {
        "hunger" -> HungerPassive(player, passiveEntry.passive)
        "double_jump" -> DoubleJumpPassive(player)
        "lightning_shield" -> LightningShieldPassive(player)
        "regeneration" -> RegenerationPassive(player, passiveEntry.passive)
        else -> throw RuntimeException("")
    }
}
