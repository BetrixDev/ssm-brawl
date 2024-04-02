package net.ssmb.kits

import net.ssmb.dtos.minigame.MinigameStartSuccess
import org.bukkit.entity.Player

interface IKit {
    val player: Player
    val kitData: MinigameStartSuccess.PlayerData.KitData

    fun initializeKit()
    fun destroyKit()
}