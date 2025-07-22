package dev.betrix.superSmashMobsBrawl.kits.instances

import dev.betrix.superSmashMobsBrawl.abilities.instances.AbilityInstance
import dev.betrix.superSmashMobsBrawl.kits.definitions.KitDefinition
import dev.betrix.superSmashMobsBrawl.passives.instances.PassiveInstance
import org.bukkit.entity.Player

open class KitInstance(
    val definition: KitDefinition,
    val player: Player
) {
    val abilityInstances = arrayListOf<AbilityInstance>()
    val passiveInstances = arrayListOf<PassiveInstance>()

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
    }

    open fun teardown() {
        abilityInstances.forEach {
            it.teardown()
            abilityInstances.remove(it)
        }

        passiveInstances.forEach {
            it.teardown()
            passiveInstances.remove(it)
        }
    }
}