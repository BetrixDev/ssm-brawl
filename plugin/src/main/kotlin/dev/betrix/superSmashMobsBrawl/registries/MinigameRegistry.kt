package dev.betrix.superSmashMobsBrawl.registries

import dev.betrix.superSmashMobsBrawl.minigames.definitions.MinigameDefinition
import dev.betrix.superSmashMobsBrawl.minigames.definitions.TestingMinigameDefinition

object MinigameRegistry {
    private val definitions = mutableMapOf<String, MinigameDefinition>()

    fun register(definition: MinigameDefinition) {
        definitions[definition.id] = definition
    }

    fun getDefinition(id: String): MinigameDefinition? = definitions[id]

    fun getAllDefinitions(): Collection<MinigameDefinition> = definitions.values

    fun findClosest(id: String): MinigameDefinition? {
        if (id.isBlank()) return null

        // First try exact match
        getDefinition(id)?.let {
            return it
        }

        // Then try case-insensitive match
        definitions.values
            .find { it.id.equals(id, ignoreCase = true) }
            ?.let {
                return it
            }

        // Finally try partial match (contains)
        return definitions.values.find { it.id.contains(id, ignoreCase = true) }
    }

    init {
        register(TestingMinigameDefinition)
    }
}
