package net.ssmb.minigames

import net.ssmb.dtos.minigame.BukkitTeamData
import net.ssmb.dtos.minigame.MinigameStartSuccess
import org.bukkit.entity.Player

fun constructMinigameFromData(teams: List<BukkitTeamData>, data: MinigameStartSuccess): IMinigame {
    return when (data.minigame.id) {
        "test_minigame" -> TestMinigame(teams, data)
        "two_player_singles" -> TwoPlayerSinglesMinigame(teams, data)
        else -> throw IllegalArgumentException("Unknown minigame id: ${data.minigame.id}")
    }
}
