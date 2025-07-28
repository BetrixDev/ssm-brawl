package dev.betrix.superSmashMobsBrawl.kits.instances

import dev.betrix.superSmashMobsBrawl.abilities.instances.AbilityInstance
import dev.betrix.superSmashMobsBrawl.di.Injectable
import dev.betrix.superSmashMobsBrawl.kits.definitions.KitDefinition
import dev.betrix.superSmashMobsBrawl.lifecycle.Manageable
import dev.betrix.superSmashMobsBrawl.passives.instances.PassiveInstance
import dev.betrix.superSmashMobsBrawl.services.HotbarService
import org.bukkit.entity.Player
import org.koin.core.component.inject

open class KitInstance(val definition: KitDefinition, val player: Player) : Manageable, Injectable {
    val abilityInstances = arrayListOf<AbilityInstance>()
    val passiveInstances = arrayListOf<PassiveInstance>()
    
    // Injected dependencies
    protected val hotbarService: HotbarService by inject()

    override fun setup() {
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

        // Setup hotbar items for abilities
        hotbarService.setupHotbarItems(this)
    }

    override fun teardown() {
        // Clear hotbar items first
        hotbarService.clearHotbarItems(player)

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
