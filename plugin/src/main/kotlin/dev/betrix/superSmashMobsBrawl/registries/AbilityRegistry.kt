package dev.betrix.superSmashMobsBrawl.registries

import dev.betrix.superSmashMobsBrawl.abilities.AbilityType
import dev.betrix.superSmashMobsBrawl.abilities.definitions.AbilityDefinition
import dev.betrix.superSmashMobsBrawl.abilities.definitions.ExplosionAbilityDefinition
import dev.betrix.superSmashMobsBrawl.abilities.definitions.LightningJumpAbilityDefinition
import dev.betrix.superSmashMobsBrawl.abilities.definitions.StealthAbilityDefinition
import dev.betrix.superSmashMobsBrawl.abilities.definitions.SulphurBombAbilityDefinition

object AbilityRegistry {
    private val definitions = mutableMapOf<String, AbilityDefinition>()

    fun register(definition: AbilityDefinition) {
        definitions[definition.id] = definition
    }

    fun getDefinition(id: String): AbilityDefinition? = definitions[id]

    fun getAllDefinitions(): Collection<AbilityDefinition> = definitions.values

    fun getDefinitionsByType(type: AbilityType): List<AbilityDefinition> {
        return definitions.values.filter { it.metadata.type == type }
    }

    init {
        register(SulphurBombAbilityDefinition)
        register(LightningJumpAbilityDefinition)
        register(ExplosionAbilityDefinition)
        register(StealthAbilityDefinition)
    }
}
