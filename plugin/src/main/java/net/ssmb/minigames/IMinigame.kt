package net.ssmb.minigames

import net.ssmb.kits.IKit
import org.bukkit.entity.Player

interface IMinigame {
    val playerKits: HashMap<Player, IKit>
    val teamsStocks: HashMap<List<Player>, Int>

    suspend fun initializeMinigame()

    fun damagePlayer(victim: Player, attacker: Player, damage: Double, knockbackMult: Double)
}