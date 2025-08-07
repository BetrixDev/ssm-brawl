package dev.betrix.superSmashMobsBrawl.kits.instances

import dev.betrix.superSmashMobsBrawl.abilities.instances.AbilityInstance
import dev.betrix.superSmashMobsBrawl.disguises.Disguise
import dev.betrix.superSmashMobsBrawl.extensions.sendDebugMessage
import dev.betrix.superSmashMobsBrawl.kits.definitions.KitDefinition
import dev.betrix.superSmashMobsBrawl.passives.instances.PassiveInstance
import dev.betrix.superSmashMobsBrawl.services.HotbarService
import org.bukkit.entity.Player

open class KitInstance(
    val definition: KitDefinition,
    val player: Player,
) {
    val abilityInstances = arrayListOf<AbilityInstance>()
    val passiveInstances = arrayListOf<PassiveInstance>()
    protected lateinit var disguise: Disguise

    open fun setup() {
        definition.metadata.abilities.forEach {
            val abilityInstance = it.createInstance(player)
            abilityInstances.add(abilityInstance)
            abilityInstance.setup()
        }

        definition.metadata.passives.forEach {
            val passiveInstance = it.createInstance(player)
            passiveInstances.add(passiveInstance)
            passiveInstance.setup()
        }

        HotbarService.setupHotbarItems(this)

        disguise.setup()

        player.sendDebugMessage("You have been given the ${definition.name} kit")
    }

    open fun teardown() {
        HotbarService.clearHotbarItems(player)

        disguise.teardown()

        abilityInstances.forEach { it.teardown() }
        abilityInstances.clear()

        passiveInstances.forEach { it.teardown() }
        passiveInstances.clear()

        player.sendDebugMessage("The ${definition.name} kit has been removed")
    }
}
