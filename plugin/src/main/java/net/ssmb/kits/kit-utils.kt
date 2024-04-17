package net.ssmb.kits

import net.ssmb.dtos.minigame.MinigameStartSuccess
import net.ssmb.minigames.IMinigame
import org.bukkit.entity.Player

fun constructKitFromData(
    player: Player,
    kitData: MinigameStartSuccess.PlayerData.KitData,
    minigame: IMinigame
): IKit {
    return when (kitData.id) {
        "creeper" -> CreeperKit(player, kitData, minigame)
        else -> throw RuntimeException("No kit exists for id ${kitData.id}")
    }
}
