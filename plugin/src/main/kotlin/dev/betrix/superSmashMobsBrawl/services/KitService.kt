package dev.betrix.superSmashMobsBrawl.services

import com.github.michaelbull.result.Err
import com.github.michaelbull.result.Ok
import com.github.michaelbull.result.Result
import dev.betrix.superSmashMobsBrawl.kits.definitions.KitDefinition
import dev.betrix.superSmashMobsBrawl.kits.instances.KitInstance
import org.bukkit.entity.Player

enum class AssignKitError {
    PLAYER_HAS_KIT
}

object KitService {
    private val assignedKits = hashMapOf<Player, KitInstance>()

    fun assignKit(player: Player, kitDefinition: KitDefinition): Result<KitInstance, AssignKitError> {
        if (assignedKits.containsKey(player)) {
            return Err(AssignKitError.PLAYER_HAS_KIT)
        }

        val kitInstance = kitDefinition.createInstance(player)

        assignedKits[player] = kitInstance

        return Ok(kitInstance)
    }

    fun unassignKit(player: Player): KitInstance? {
        return assignedKits.remove(player)
    }

    fun unassignKit(kitInstance: KitInstance) {
        val entry = assignedKits.filterValues { it == kitInstance }.keys.firstOrNull()

        if (entry == null) {
            return
        }

        assignedKits.remove(entry)
    }
}