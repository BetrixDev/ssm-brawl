package dev.betrix.superSmashMobsBrawl.kits.instances

import dev.betrix.superSmashMobsBrawl.abilities.instances.AbilityInstance
import dev.betrix.superSmashMobsBrawl.disguises.Disguise
import dev.betrix.superSmashMobsBrawl.extensions.sendDebugMessage
import dev.betrix.superSmashMobsBrawl.kits.definitions.KitDefinition
import dev.betrix.superSmashMobsBrawl.minigames.definitions.MinigameDefinition
import dev.betrix.superSmashMobsBrawl.passives.instances.PassiveInstance
import dev.betrix.superSmashMobsBrawl.services.HotbarService
import org.bukkit.entity.Player

open class KitInstance(
    val definition: KitDefinition,
    val player: Player,
    private val minigameDefinition: MinigameDefinition? = null,
) {
    val abilityInstances = arrayListOf<AbilityInstance>()
    val passiveInstances = arrayListOf<PassiveInstance>()
    protected lateinit var disguise: Disguise

    open fun setup() {
        // Filter out blacklisted abilities
        val allowedAbilities =
            definition.metadata.abilities.filter { ability ->
                when (minigameDefinition?.blacklistedAbilities?.contains(ability) == true) {
                    true -> {
                        player.sendDebugMessage(
                            "[Kit] Ability <light_purple>${ability.name}</light_purple> was blacklisted from this minigame"
                        )
                        false
                    }
                    false -> {
                        true
                    }
                }
            }

        allowedAbilities.forEach {
            val abilityInstance = it.createInstance(player)
            abilityInstances.add(abilityInstance)
            abilityInstance.setup()
        }

        // Filter out blacklisted passives
        val allowedPassives =
            definition.metadata.passives.filter { passive ->
                when (minigameDefinition?.blacklistedPassives?.contains(passive) == true) {
                    true -> {
                        player.sendDebugMessage(
                            "[Kit] Passive <light_purple>${passive.name}</light_purple> was blacklisted from this minigame"
                        )
                        false
                    }
                    false -> {
                        true
                    }
                }
            }

        allowedPassives.forEach {
            val passiveInstance = it.createInstance(player)
            passiveInstances.add(passiveInstance)
            passiveInstance.setup()
        }

        // Setup hotbar items for abilities
        HotbarService.setupHotbarItems(this)

        disguise.setup()

        player.sendDebugMessage("You have been given the ${definition.name} kit")
    }

    open fun teardown() {
        // Clear hotbar items first
        HotbarService.clearHotbarItems(player)

        disguise.teardown()

        abilityInstances.forEach { it.teardown() }
        abilityInstances.clear()

        passiveInstances.forEach { it.teardown() }
        passiveInstances.clear()

        player.sendDebugMessage("The ${definition.name} kit has been removed")
    }
}
