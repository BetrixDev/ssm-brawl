package dev.betrix.superSmashMobsBrawl.registries

import dev.betrix.superSmashMobsBrawl.passives.definitions.DoubleJumpPassiveDefinition
import dev.betrix.superSmashMobsBrawl.passives.definitions.ExplosiveFeedbackPassiveDefinition
import dev.betrix.superSmashMobsBrawl.passives.definitions.PassiveDefinition

object PassiveRegistry {
    private val definitions = mutableMapOf<String, PassiveDefinition>()

    fun register(definition: PassiveDefinition) {
        definitions[definition.id] = definition
    }

    fun getDefinition(id: String): PassiveDefinition? = definitions[id]

    fun getAllDefinitions(): Collection<PassiveDefinition> = definitions.values

    init {
        register(DoubleJumpPassiveDefinition)
        register(ExplosiveFeedbackPassiveDefinition)
    }
}
