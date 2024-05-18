package net.ssmb.minigames

import net.ssmb.dtos.minigame.BukkitTeamData
import net.ssmb.kits.SsmbKit
import org.bukkit.Bukkit
import org.bukkit.entity.Player

interface IMinigame {
    val teams: List<BukkitTeamData>
    val playerKits: HashMap<Player, SsmbKit>
    val teamsStocks: HashMap<String, Int>

    suspend fun initializeMinigame()

    fun removePlayer(player: Player)
}
