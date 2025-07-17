package dev.betrix.superSmashMobsBrawl.registries
import dev.betrix.superSmashMobsBrawl.kits.definitions.CreeperKitDefinition
import dev.betrix.superSmashMobsBrawl.kits.definitions.KitDefinition

object KitRegistry {
    private val definitions = mutableMapOf<String, KitDefinition>()

    fun register(definition: KitDefinition) {
        definitions[definition.id] = definition
    }

    fun getDefinition(id: String): KitDefinition? = definitions[id]

    fun getAllDefinitions(): Collection<KitDefinition> = definitions.values

    init {
        register(CreeperKitDefinition)
    }
}