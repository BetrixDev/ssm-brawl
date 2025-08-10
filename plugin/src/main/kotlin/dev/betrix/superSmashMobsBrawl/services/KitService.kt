package dev.betrix.superSmashMobsBrawl.services

import com.github.michaelbull.result.Err
import com.github.michaelbull.result.Ok
import com.github.michaelbull.result.Result
import com.github.shynixn.mccoroutine.bukkit.launch
import dev.betrix.superSmashMobsBrawl.kits.BrawlKit
import java.util.concurrent.ConcurrentHashMap
import org.bukkit.entity.Player
import org.bukkit.plugin.java.JavaPlugin
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

enum class AssignKitError {
    PLAYER_HAS_KIT
}

object KitService : KoinComponent {
    private val playerSelectedKits = ConcurrentHashMap<Player, String>() // kit id
    private val assignedBrawlKits = ConcurrentHashMap<Player, BrawlKit>()

    private val plugin: JavaPlugin by inject()
    private val dataService: DataService by inject()

    fun playerSelectKit(player: Player, kitId: String) {
        playerSelectedKits[player] = kitId
    }

    private fun defaultKitId(): String {
        return dataService.getKit("creeper")?.id ?: dataService.getKit("skeleton")?.id ?: "creeper"
    }

    fun assignKit(player: Player): Result<BrawlKit, AssignKitError> {
        if (!playerSelectedKits.containsKey(player) || playerSelectedKits[player] == null) {
            playerSelectedKits[player] = defaultKitId()
        }
        return assignKit(player, playerSelectedKits[player] ?: defaultKitId())
    }

    fun assignKit(player: Player, kitId: String): Result<BrawlKit, AssignKitError> {
        if (assignedBrawlKits.containsKey(player)) {
            return Err(AssignKitError.PLAYER_HAS_KIT)
        }

        val kitData = dataService.getKit(kitId) ?: dataService.getKit(defaultKitId())!!

        val brawlKit =
            when (kitData.id) {
                else -> BrawlKit(kitData.id, player)
            }

        assignedBrawlKits[player] = brawlKit
        brawlKit.setup()
        return Ok(brawlKit)
    }

    fun unassignKit(player: Player): BrawlKit? {
        val kit = assignedBrawlKits.remove(player)
        kit?.let { instance ->
            plugin.launch {
                try {
                    instance.teardown()
                } catch (e: Exception) {
                    plugin.logger.warning(
                        "Error during kit teardown for player ${player.name}: ${e.message}"
                    )
                }
            }
        }
        return kit
    }

    fun getKitForPlayer(player: Player): BrawlKit? = assignedBrawlKits[player]

    fun hasKit(player: Player): Boolean = assignedBrawlKits.containsKey(player)
}
