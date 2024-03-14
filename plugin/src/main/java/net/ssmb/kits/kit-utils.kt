package net.ssmb.kits

import net.ssmb.dtos.minigame.MinigameStartSuccess
import org.bukkit.entity.Player

fun constructKitFromData(player: Player, kitData: MinigameStartSuccess.PlayerData.KitData): IKit {
    return when (kitData.id) {
        "creeper" -> CreeperKit(player, kitData)
        else -> throw RuntimeException("No kit exists for id ${kitData.id}")
    }
}