package dev.betrix.superSmashMobsBrawl.registries

import dev.betrix.superSmashMobsBrawl.kits.definitions.KitDefinition
import dev.betrix.superSmashMobsBrawl.factories.DefinitionFactory
import dev.betrix.superSmashMobsBrawl.services.DataService
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

object KitRegistry : KoinComponent {
    private val dataService: DataService by inject()
    private val definitions = mutableMapOf<String, KitDefinition>()

    fun register(definition: KitDefinition) {
        definitions[definition.id] = definition
    }

    fun getDefinition(id: String): KitDefinition? {
        // First check if already loaded
        definitions[id]?.let { return it }
        
        // Try to load from data
        val kitData = dataService.getKit(id) ?: return null
        val definition = DefinitionFactory.createKitDefinition(kitData)
        register(definition)
        return definition
    }

    fun getAllDefinitions(): Collection<KitDefinition> {
        // Load all kits from data if not already loaded
        loadAllFromData()
        return definitions.values
    }
    
    private fun loadAllFromData() {
        // This will be called after DataService is initialized
        try {
            dataService.getAllKitIds().forEach { id ->
                if (!definitions.containsKey(id)) {
                    getDefinition(id) // This will load and register it
                }
            }
        } catch (e: Exception) {
            // DataService might not be initialized yet
        }
    }
}
