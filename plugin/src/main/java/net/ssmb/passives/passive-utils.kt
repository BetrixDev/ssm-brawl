package net.ssmb.passives

import net.ssmb.dtos.minigame.MinigameStartSuccess
import org.bukkit.entity.Player

fun constructPassiveFromData(
    player: Player,
    passiveEntry: MinigameStartSuccess.PlayerData.KitData.PassiveEntry
): SsmbPassive {
    val passiveData = passiveEntry.passive

    return when (passiveData.id) {
        "hunger" -> HungerPassive(player, passiveData)
        "double_jump" -> DoubleJumpPassive(player, passiveData)
        "lightning_shield" -> LightningShieldPassive(player, passiveData)
        "regeneration" -> RegenerationPassive(player, passiveData)
        "stampede" -> StampedePassive(player, passiveData)
        else -> throw RuntimeException("No passive exists for ${passiveData.id}")
    }
}
