package dev.betrix.superSmashMobsBrawl.registries

import dev.betrix.superSmashMobsBrawl.minigames.definitions.MinigameDefinition
import dev.betrix.superSmashMobsBrawl.factories.DefinitionFactory
import dev.betrix.superSmashMobsBrawl.services.DataService
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

object MinigameRegistry : KoinComponent {
    private val dataService: DataService by inject()
    private val definitions = mutableMapOf<String, MinigameDefinition>()

    fun register(definition: MinigameDefinition) {
        definitions[definition.id] = definition
    }

    fun getDefinition(id: String): MinigameDefinition? {
        // First check if already loaded
        definitions[id]?.let { return it }
        
        // Try to load from data
        val minigameData = dataService.getMinigame(id) ?: return null
        val definition = DefinitionFactory.createMinigameDefinition(minigameData)
        register(definition)
        return definition
    }

    fun getAllDefinitions(): Collection<MinigameDefinition> {
        // Load all minigames from data if not already loaded
        loadAllFromData()
        return definitions.values
    }

    fun findClosest(id: String): MinigameDefinition? {
        if (id.isBlank()) return null

        // First try exact match
        getDefinition(id)?.let {
            return it
        }

        // Load all to search through them
        loadAllFromData()

        // Then try case-insensitive match
        definitions.values
            .find { it.id.equals(id, ignoreCase = true) }
            ?.let {
                return it
            }

        // Finally try partial match (contains)
        return definitions.values.find { it.id.contains(id, ignoreCase = true) }
    }
    
    private fun loadAllFromData() {
        try {
            dataService.getAllMinigameIds().forEach { id ->
                if (!definitions.containsKey(id)) {
                    getDefinition(id) // This will load and register it
                }
            }
        } catch (e: Exception) {
            // DataService might not be initialized yet
        }
    }
}
