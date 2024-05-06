package net.ssmb.minigames

import net.ssmb.dtos.minigame.MinigameStartSuccess
import org.bukkit.entity.Player

fun constructMinigameFromData(players: List<List<Player>>, data: MinigameStartSuccess): IMinigame {
    return when (data.minigame.id) {
        "test_minigame" -> TestMinigame(players, data)
        "two_player_singles" -> TwoPlayerSinglesMinigame(players, data)
        else -> throw IllegalArgumentException("Unknown minigame id: ${data.minigame.id}")
    }
}
