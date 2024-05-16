package net.ssmb.kits

import net.ssmb.dtos.minigame.MinigameStartSuccess
import net.ssmb.minigames.IMinigame
import org.bukkit.entity.Player

class CreeperKit(
    player: Player,
    kitData: MinigameStartSuccess.PlayerData.KitData,
    minigame: IMinigame?
) : SsmbKit(player, kitData, minigame) {}
