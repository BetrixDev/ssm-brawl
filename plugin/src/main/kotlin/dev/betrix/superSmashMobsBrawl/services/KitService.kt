package dev.betrix.superSmashMobsBrawl.services

import com.github.michaelbull.result.Err
import com.github.michaelbull.result.Ok
import com.github.michaelbull.result.Result
import dev.betrix.superSmashMobsBrawl.kits.definitions.CreeperKitDefinition
import dev.betrix.superSmashMobsBrawl.kits.definitions.KitDefinition
import dev.betrix.superSmashMobsBrawl.kits.instances.KitInstance
import org.bukkit.entity.Player

enum class AssignKitError {
    PLAYER_HAS_KIT
}

object KitService {
    private val playerSelectedKits = hashMapOf<Player, KitDefinition>()
    private val assignedKits = hashMapOf<Player, KitInstance>()

    fun playerSelectKit(player: Player, kitDefinition: KitDefinition) {
        playerSelectedKits[player] = kitDefinition
    }

    fun assignKit(player: Player): Result<KitInstance, AssignKitError> {
        if (!playerSelectedKits.containsKey(player) && playerSelectedKits[player] != null) {
            playerSelectedKits[player] = CreeperKitDefinition // Default kit for now
        }

        return assignKit(player, playerSelectedKits[player]!!)
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
        val kitInstance = assignedKits.remove(player)
        kitInstance?.teardown()
        return kitInstance
    }

    fun unassignKit(kitInstance: KitInstance) {
        val entry = assignedKits.filterValues { it == kitInstance }.keys.firstOrNull()

        if (entry == null) {
            return
        }

        assignedKits.remove(entry)
        kitInstance.teardown()
    }

    fun getKitInstance(player: Player): KitInstance? {
        return assignedKits[player]
    }

    fun hasKit(player: Player): Boolean {
        return assignedKits.containsKey(player)
    }
}
