package dev.betrix.superSmashMobsBrawl.services

import com.github.michaelbull.result.Err
import com.github.michaelbull.result.Ok
import com.github.michaelbull.result.Result
import com.github.shynixn.mccoroutine.bukkit.launch
import dev.betrix.superSmashMobsBrawl.SuperSmashMobsBrawl
import dev.betrix.superSmashMobsBrawl.abilities.AbilitySpec
import dev.betrix.superSmashMobsBrawl.kits.KitSpec
import dev.betrix.superSmashMobsBrawl.kits.KitType
import dev.betrix.superSmashMobsBrawl.kits.definitions.KitDefinition
import dev.betrix.superSmashMobsBrawl.kits.instances.KitInstance
import dev.betrix.superSmashMobsBrawl.models.brawlData.KitDef
import dev.betrix.superSmashMobsBrawl.passives.PassiveSpec
import java.util.concurrent.ConcurrentHashMap
import org.bukkit.entity.Player
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

enum class AssignKitError {
    PLAYER_HAS_KIT
}

object KitService : KoinComponent {
    private val playerSelectedKits = ConcurrentHashMap<Player, String>() // kit id
    private val assignedKits = ConcurrentHashMap<Player, KitInstance>()

    private val plugin = SuperSmashMobsBrawl.instance
    private val dataService: DataService by inject()

    fun playerSelectKit(player: Player, kitId: String) {
        playerSelectedKits[player] = kitId
    }

    fun playerSelectKit(player: Player, kitDefinition: KitDefinition) {
        playerSelectedKits[player] = kitDefinition.id
    }

    private fun defaultKitId(): String {
        return dataService.getKit("creeper")?.id ?: dataService.getKit("skeleton")?.id ?: "creeper"
    }

    fun assignKit(player: Player): Result<KitInstance, AssignKitError> {
        if (!playerSelectedKits.containsKey(player) || playerSelectedKits[player] == null) {
            playerSelectedKits[player] = defaultKitId()
        }

        return assignKit(player, playerSelectedKits[player] ?: defaultKitId())
    }

    fun assignKit(
        player: Player,
        kitId: String,
    ): Result<KitInstance, AssignKitError> {
        if (assignedKits.containsKey(player)) {
            return Err(AssignKitError.PLAYER_HAS_KIT)
        }

        val kitData = dataService.getKit(kitId) ?: dataService.getKit(defaultKitId())!!
        val kitSpec = buildKitSpec(kitData)

        // TODO: Replace KitInstance with data-driven instance; for now reuse existing KitInstance
        val legacyDefinition =
            if (kitId == "skeleton")
                dev.betrix.superSmashMobsBrawl.kits.definitions.SkeletonKitDefinition
            else dev.betrix.superSmashMobsBrawl.kits.definitions.CreeperKitDefinition
        val kitInstance = legacyDefinition.createInstance(player)

        assignedKits[player] = kitInstance

        kitInstance.setup()

        return Ok(kitInstance)
    }

    fun assignKit(player: Player, kitDefinition: KitDefinition): Result<KitInstance, AssignKitError> {
        return assignKit(player, kitDefinition.id)
    }

    fun unassignKit(player: Player): KitInstance? {
        val kitInstance = assignedKits.remove(player)

        kitInstance?.let { instance ->
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

        return kitInstance
    }

    fun unassignKit(kitInstance: KitInstance) {
        val playerToRemove = assignedKits.entries.find { it.value == kitInstance }?.key

        playerToRemove?.let { player ->
            assignedKits.remove(player)

            plugin.launch {
                try {
                    kitInstance.teardown()
                } catch (e: Exception) {
                    plugin.logger.warning(
                        "Error during kit teardown for player ${player.name}: ${e.message}"
                    )
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

    private fun buildKitSpec(kit: KitDef): KitSpec {
        val name = kit.id.replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }

        val passive = dataService.getPassive(kit.passives.id)
        val passiveSpec = if (passive != null) {
            val def = dev.betrix.superSmashMobsBrawl.registries.PassiveRegistry.getDefinition(passive.id)
            PassiveSpec(
                id = passive.id,
                name = def?.name ?: passive.id,
                metadata = dev.betrix.superSmashMobsBrawl.passives.PassiveMetadata(
                    description = passive.metadata?.get("description")?.toString() ?: "",
                    userFacing = passive.userFacing,
                ),
            )
        } else null

        val ability = dataService.getAbility(kit.abilities.id)
        val abilitySpec = if (ability != null) {
            val def = dev.betrix.superSmashMobsBrawl.registries.AbilityRegistry.getDefinition(ability.id)
            AbilitySpec(
                id = ability.id,
                name = def?.name ?: ability.id,
                metadata = dev.betrix.superSmashMobsBrawl.abilities.AbilityMetadata(
                    description = "",
                    type = when (ability.type.name) {
                        "AOE" -> dev.betrix.superSmashMobsBrawl.abilities.AbilityType.AOE
                        else -> dev.betrix.superSmashMobsBrawl.abilities.AbilityType.PROJECTILE
                    },
                    cooldown = ability.cooldown.toInt(),
                    hotbarItem = def?.metadata?.hotbarItem ?: def!!.metadata.hotbarItem,
                    hotbarItemSlot = ability.itemSlot,
                    usageType = when (ability.usage.name) {
                        "RIGHT_CLICK" -> dev.betrix.superSmashMobsBrawl.abilities.AbilityUsageType.RIGHT_CLICK
                        else -> dev.betrix.superSmashMobsBrawl.abilities.AbilityUsageType.LEFT_CLICK
                    },
                ),
            )
        } else null

        return KitSpec(
            id = kit.id,
            name = name,
            description = "",
            type = KitType.DEFAULT,
            meleeDamage = kit.meleeDamage.toInt(),
            passives = listOfNotNull(passiveSpec),
            abilities = listOfNotNull(abilitySpec),
        )
    }
}
