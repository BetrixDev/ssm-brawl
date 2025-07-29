package dev.betrix.superSmashMobsBrawl.registries

import dev.betrix.superSmashMobsBrawl.passives.definitions.DoubleJumpPassiveDefinition
import dev.betrix.superSmashMobsBrawl.passives.definitions.HungerPassiveDefinition
import dev.betrix.superSmashMobsBrawl.passives.definitions.PassiveDefinition
import dev.betrix.superSmashMobsBrawl.passives.definitions.RegenerationPassiveDefinition

object PassiveRegistry {
    private val definitions = mutableMapOf<String, PassiveDefinition>()

    fun register(definition: PassiveDefinition) {
        definitions[definition.id] = definition
    }

    fun getDefinition(id: String): PassiveDefinition? = definitions[id]

    fun getAllDefinitions(): Collection<PassiveDefinition> = definitions.values

    init {
        register(DoubleJumpPassiveDefinition)
        register(HungerPassiveDefinition)
        register(RegenerationPassiveDefinition)
    }
}
