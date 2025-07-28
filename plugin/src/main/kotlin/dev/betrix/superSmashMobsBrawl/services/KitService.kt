package dev.betrix.superSmashMobsBrawl.services

import com.github.michaelbull.result.Err
import com.github.michaelbull.result.Ok
import com.github.michaelbull.result.Result
import dev.betrix.superSmashMobsBrawl.SuperSmashMobsBrawl
import dev.betrix.superSmashMobsBrawl.di.Injectable
import dev.betrix.superSmashMobsBrawl.kits.definitions.KitDefinition
import dev.betrix.superSmashMobsBrawl.kits.instances.KitInstance
import dev.betrix.superSmashMobsBrawl.lifecycle.Manageable
import org.bukkit.entity.Player
import org.koin.core.component.inject

enum class AssignKitError {
    PLAYER_HAS_KIT
}

object KitService : Manageable, Injectable {
    private val plugin: SuperSmashMobsBrawl by inject()
    private val assignedKits = hashMapOf<Player, KitInstance>()

    fun assignKit(
        player: Player,
        kitDefinition: KitDefinition,
    ): Result<KitInstance, AssignKitError> {
        if (assignedKits.containsKey(player)) {
            return Err(AssignKitError.PLAYER_HAS_KIT)
        }

        val kitInstance = kitDefinition.createInstance(player)
        assignedKits[player] = kitInstance

        // Setup the kit (which will also setup hotbar items)
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
    
    override fun teardown() {
        // Teardown all assigned kits
        assignedKits.values.forEach { it.teardown() }
        assignedKits.clear()
        plugin.logger.info("Kit service cleaned up")
    }
}
