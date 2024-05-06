package net.ssmb.minigames

import net.ssmb.kits.IKit
import org.bukkit.entity.Player

interface IMinigame {
    val teams: List<List<Player>>
    val playerKits: HashMap<Player, IKit>
    val teamsStocks: ArrayList<Pair<ArrayList<Player>, Int>>

    suspend fun initializeMinigame()

    fun removePlayer(player: Player)
}
