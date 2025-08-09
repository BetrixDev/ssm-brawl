package dev.betrix.superSmashMobsBrawl.services

import com.github.michaelbull.result.Err
import com.github.michaelbull.result.Ok
import com.github.michaelbull.result.Result
import com.github.shynixn.mccoroutine.bukkit.launch
import dev.betrix.superSmashMobsBrawl.SuperSmashMobsBrawl
import dev.betrix.superSmashMobsBrawl.abilities.AbilityMetadata
import dev.betrix.superSmashMobsBrawl.abilities.AbilityType
import dev.betrix.superSmashMobsBrawl.abilities.AbilityUsageType
import dev.betrix.superSmashMobsBrawl.brawl.BrawlAbility
import dev.betrix.superSmashMobsBrawl.brawl.BrawlKit
import dev.betrix.superSmashMobsBrawl.brawl.registries.BrawlAbilityRegistry
import dev.betrix.superSmashMobsBrawl.brawl.registry.BrawlPassiveRegistry
import dev.betrix.superSmashMobsBrawl.disguises.CreeperDisguise
import dev.betrix.superSmashMobsBrawl.disguises.SkeletonDisguise
import dev.betrix.superSmashMobsBrawl.models.brawlData.AbilityDef
import dev.betrix.superSmashMobsBrawl.models.brawlData.MinigameDef
import dev.betrix.superSmashMobsBrawl.passives.PassiveMetadata
import dev.betrix.superSmashMobsBrawl.utils.itemFromString
import java.util.concurrent.ConcurrentHashMap
import org.bukkit.Material
import org.bukkit.entity.Player
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

enum class AssignKitError {
    PLAYER_HAS_KIT
}

object KitService : KoinComponent {
    private val playerSelectedKits = ConcurrentHashMap<Player, String>() // kit id
    private val assignedBrawlKits = ConcurrentHashMap<Player, BrawlKit>()

    private val plugin = SuperSmashMobsBrawl.instance
    private val dataService: DataService by inject()

    fun playerSelectKit(player: Player, kitId: String) {
        playerSelectedKits[player] = kitId
    }

    private fun defaultKitId(): String {
        return dataService.getKit("creeper")?.id ?: dataService.getKit("skeleton")?.id ?: "creeper"
    }

    fun assignKit(player: Player, minigameDef: MinigameDef? = null): Result<BrawlKit, AssignKitError> {
        if (!playerSelectedKits.containsKey(player) || playerSelectedKits[player] == null) {
            playerSelectedKits[player] = defaultKitId()
        }
        return assignKit(player, playerSelectedKits[player] ?: defaultKitId(), minigameDef)
    }

    fun assignKit(player: Player, kitId: String, minigameDef: MinigameDef? = null): Result<BrawlKit, AssignKitError> {
        if (assignedBrawlKits.containsKey(player)) {
            return Err(AssignKitError.PLAYER_HAS_KIT)
        }

        val kitData = dataService.getKit(kitId) ?: dataService.getKit(defaultKitId())!!
        val brawlKit = BrawlKit(kitData.id, player)

        when (kitData.id.lowercase()) {
            "skeleton" -> brawlKit.applyDisguise(SkeletonDisguise(player))
            "creeper" -> brawlKit.applyDisguise(CreeperDisguise(player))
        }

        kitData.abilities.forEach { kitAbilityDef ->
            val abilityDef = dataService.getAbility(kitAbilityDef.id) ?: return@forEach
            val abilityMeta = buildAbilityMetadata(abilityDef)
            val ability: BrawlAbility? =
                BrawlAbilityRegistry.create(abilityDef.id, player, abilityMeta)
            if (ability != null) {
                brawlKit.abilities.add(ability)
            } else {
                plugin.logger.warning("No BrawlAbility registered for id '${abilityDef.id}'")
            }
        }

        kitData.passives.forEach { kitPassiveDef ->
            if (!canPassiveBeUsedInMinigame(kitPassiveDef.id, minigameDef)) {
                return@forEach
            }

            val passiveDef = dataService.getPassive(kitPassiveDef.id) ?: return@forEach
            val metadata =
                PassiveMetadata(
                    description = passiveDef.metadata?.get("description")?.toString() ?: "",
                    userFacing = passiveDef.userFacing,
                )
            val overrideMap =
                kitPassiveDef.overrides?.metadata?.mapValues { it.value.toString() } ?: emptyMap()
            val passive = BrawlPassiveRegistry.create(passiveDef.id, player, metadata, overrideMap)
            if (passive != null) {
                brawlKit.passives.add(passive)
            } else {
                plugin.logger.warning("No BrawlPassive registered for id '${passiveDef.id}'")
            }
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

    fun getBrawlKit(player: Player): BrawlKit? = assignedBrawlKits[player]

    fun hasKit(player: Player): Boolean = assignedBrawlKits.containsKey(player)

    private fun canPassiveBeUsedInMinigame(id: String, minigameDef: MinigameDef? = null): Boolean {
        if (minigameDef == null) {
            return true
        }

        if (minigameDef.passiveBlacklist != null && minigameDef.passiveBlacklist?.contains(id) == true) {
            return false
        }

        if (minigameDef.passiveWhitelist != null && minigameDef.passiveWhitelist?.contains(id) == false) {
            return false
        }

        return true
    }

    private fun buildAbilityMetadata(ability: AbilityDef): AbilityMetadata {
        val usage =
            when (ability.usage.name.uppercase()) {
                "RIGHT_CLICK" -> AbilityUsageType.RIGHT_CLICK
                else -> AbilityUsageType.LEFT_CLICK
            }
        val type =
            when (ability.type.name.uppercase()) {
                "AOE" -> AbilityType.AOE
                else -> AbilityType.PROJECTILE
            }
        val hotbarItem = itemFromString(ability.hotbarItem, Material.IRON_AXE)
        return AbilityMetadata(
            description = "",
            type = type,
            cooldown = ability.cooldown.toInt(),
            hotbarItem = hotbarItem,
            hotbarItemSlot = ability.itemSlot,
            usageType = usage,
        )
    }
}
