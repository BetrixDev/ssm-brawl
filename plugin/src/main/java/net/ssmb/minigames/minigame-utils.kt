package net.ssmb.minigames

import net.ssmb.dtos.minigame.MinigameStartSuccess
import org.bukkit.entity.Player

fun constructMinigameFromData(players: List<Player>, data: MinigameStartSuccess): IMinigame {
    return when (data.minigame.id) {
        "test-minigame" -> TestMinigame(players, data)
        else -> throw IllegalArgumentException("Unknown minigame id: ${data.minigame.id}")
    }
}