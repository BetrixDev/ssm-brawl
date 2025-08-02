package dev.betrix.superSmashMobsBrawl.services

import com.github.michaelbull.result.Err
import com.github.michaelbull.result.Ok
import com.github.michaelbull.result.Result
import com.github.shynixn.mccoroutine.bukkit.launch
import dev.betrix.superSmashMobsBrawl.SuperSmashMobsBrawl
import dev.betrix.superSmashMobsBrawl.kits.definitions.CreeperKitDefinition
import dev.betrix.superSmashMobsBrawl.kits.definitions.KitDefinition
import dev.betrix.superSmashMobsBrawl.kits.instances.KitInstance
import org.bukkit.entity.Player
import java.util.concurrent.ConcurrentHashMap

enum class AssignKitError {
    PLAYER_HAS_KIT
}

object KitService {
    private val playerSelectedKits = ConcurrentHashMap<Player, KitDefinition>()
    private val assignedKits = ConcurrentHashMap<Player, KitInstance>()

    private val plugin = SuperSmashMobsBrawl.instance

    fun playerSelectKit(player: Player, kitDefinition: KitDefinition) {
        playerSelectedKits[player] = kitDefinition
    }

    fun assignKit(player: Player): Result<KitInstance, AssignKitError> {
        if (!playerSelectedKits.containsKey(player) || playerSelectedKits[player] == null) {
            playerSelectedKits[player] = CreeperKitDefinition // Default kit for now
        }

        return assignKit(player, playerSelectedKits[player] ?: CreeperKitDefinition)
    }

    fun assignKit(
        player: Player,
        kitDefinition: KitDefinition,
    ): Result<KitInstance, AssignKitError> {
        if (assignedKits.containsKey(player)) {
            return Err(AssignKitError.PLAYER_HAS_KIT)
        }

        val kitInstance = kitDefinition.createInstance(player)
        assignedKits[player] = kitInstance

        // Set up the kit (which will also set up hotbar items)
        kitInstance.setup()

        return Ok(kitInstance)
    }

    fun unassignKit(player: Player): KitInstance? {
        // Remove the kit instance immediately to prevent concurrent access
        val kitInstance = assignedKits.remove(player)

        // Run teardown asynchronously to prevent concurrent modification
        kitInstance?.let { instance ->
            // Assuming you have access to your plugin's coroutine scope
            // You might need to adjust this based on your mccoroutine setup
            plugin.launch {
                try {
                    instance.teardown()
                } catch (e: Exception) {
                    // Handle any exceptions during teardown
                    plugin.logger.warning("Error during kit teardown for player ${player.name}: ${e.message}")
                }
            }
        }

        return kitInstance
    }

    fun unassignKit(kitInstance: KitInstance) {
        // Thread-safe way to find and remove the kit instance
        val playerToRemove = assignedKits.entries.find { it.value == kitInstance }?.key

        playerToRemove?.let { player ->
            assignedKits.remove(player)

            // Run teardown asynchronously
            plugin.launch {
                try {
                    kitInstance.teardown()
                } catch (e: Exception) {
                    plugin.logger.warning("Error during kit teardown for player ${player.name}: ${e.message}")
                }
            }
        }
    }

    fun getKitInstance(player: Player): KitInstance? {
        return assignedKits[player]
    }

    fun hasKit(player: Player): Boolean {
        return assignedKits.containsKey(player)
    }
}