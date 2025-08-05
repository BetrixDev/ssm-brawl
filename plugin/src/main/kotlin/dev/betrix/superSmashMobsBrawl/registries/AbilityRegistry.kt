package dev.betrix.superSmashMobsBrawl.registries

import dev.betrix.superSmashMobsBrawl.abilities.AbilityType
import dev.betrix.superSmashMobsBrawl.abilities.definitions.AbilityDefinition
import dev.betrix.superSmashMobsBrawl.factories.DefinitionFactory
import dev.betrix.superSmashMobsBrawl.services.DataService
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

object AbilityRegistry : KoinComponent {
    private val dataService: DataService by inject()
    private val definitions = mutableMapOf<String, AbilityDefinition>()

    fun register(definition: AbilityDefinition) {
        definitions[definition.id] = definition
    }

    fun getDefinition(id: String): AbilityDefinition? {
        // First check if already loaded
        definitions[id]?.let { return it }
        
        // Try to load from data
        val abilityData = dataService.getAbility(id) ?: return null
        val definition = DefinitionFactory.createAbilityDefinition(abilityData)
        register(definition)
        return definition
    }

    fun getAllDefinitions(): Collection<AbilityDefinition> {
        // Load all abilities from data if not already loaded
        loadAllFromData()
        return definitions.values
    }

    fun getDefinitionsByType(type: AbilityType): List<AbilityDefinition> {
        loadAllFromData()
        return definitions.values.filter { it.metadata.type == type }
    }
    
    private fun loadAllFromData() {
        try {
            dataService.getAllAbilityIds().forEach { id ->
                if (!definitions.containsKey(id)) {
                    getDefinition(id) // This will load and register it
                }
            }
        } catch (e: Exception) {
            // DataService might not be initialized yet
        }
    }
}
