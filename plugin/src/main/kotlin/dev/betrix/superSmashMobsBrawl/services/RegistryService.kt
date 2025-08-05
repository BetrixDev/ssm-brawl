package dev.betrix.superSmashMobsBrawl.services

import dev.betrix.superSmashMobsBrawl.registries.AbilityRegistry
import dev.betrix.superSmashMobsBrawl.registries.KitRegistry
import dev.betrix.superSmashMobsBrawl.registries.MinigameRegistry
import dev.betrix.superSmashMobsBrawl.registries.PassiveRegistry
import org.koin.core.component.KoinComponent

object RegistryService : KoinComponent {
    
    /**
     * Initialize all registries after DataService has loaded the data
     */
    fun initializeRegistries() {
        // Force load all definitions from data
        KitRegistry.getAllDefinitions()
        AbilityRegistry.getAllDefinitions()
        PassiveRegistry.getAllDefinitions()
        MinigameRegistry.getAllDefinitions()
    }
}