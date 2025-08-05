package dev.betrix.superSmashMobsBrawl.registries

import dev.betrix.superSmashMobsBrawl.passives.definitions.PassiveDefinition
import dev.betrix.superSmashMobsBrawl.factories.DefinitionFactory
import dev.betrix.superSmashMobsBrawl.services.DataService
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

object PassiveRegistry : KoinComponent {
    private val dataService: DataService by inject()
    private val definitions = mutableMapOf<String, PassiveDefinition>()

    fun register(definition: PassiveDefinition) {
        definitions[definition.id] = definition
    }

    fun getDefinition(id: String): PassiveDefinition? {
        // First check if already loaded
        definitions[id]?.let { return it }
        
        // Try to load from data
        val passiveData = dataService.getPassive(id) ?: return null
        val definition = DefinitionFactory.createPassiveDefinition(passiveData)
        register(definition)
        return definition
    }

    fun getAllDefinitions(): Collection<PassiveDefinition> {
        // Load all passives from data if not already loaded
        loadAllFromData()
        return definitions.values
    }
    
    private fun loadAllFromData() {
        try {
            dataService.getAllPassiveIds().forEach { id ->
                if (!definitions.containsKey(id)) {
                    getDefinition(id) // This will load and register it
                }
            }
        } catch (e: Exception) {
            // DataService might not be initialized yet
        }
    }
}
