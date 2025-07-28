package dev.betrix.superSmashMobsBrawl.kits

import dev.betrix.superSmashMobsBrawl.abilities.definitions.AbilityDefinition
import dev.betrix.superSmashMobsBrawl.passives.definitions.HungerPassiveDefinition
import dev.betrix.superSmashMobsBrawl.passives.definitions.PassiveDefinition
import dev.betrix.superSmashMobsBrawl.passives.definitions.RegenerationPassiveDefinition

data class KitMetadata(
    val description: String = "",
    val type: KitType = KitType.DEFAULT,
    val meleeDamage: Int = 0,
    val passives: List<PassiveDefinition>,
    val abilities: List<AbilityDefinition>,
    val regenerationRate: Double = 0.5,
    val maxHealth: Double = 20.0,
    val regenerationDelay: Int = 100
)

enum class KitType {
    DEFAULT
}

class Kit {
    var description = ""
    var type = KitType.DEFAULT
    var meleeDamage = 0
    var regenerationRate = 0.5
    var maxHealth = 20.0
    var regenerationDelay = 100
    val passives = arrayListOf<PassiveDefinition>()
    val abilities = arrayListOf<AbilityDefinition>()

    fun passive(id: PassiveDefinition): Kit {
        passives.add(id)

        return this
    }

    fun ability(id: AbilityDefinition): Kit {
        abilities.add(id)

        return this
    }

    /**
     * Adds the core SSMB passives (Hunger and Regeneration) with kit-specific settings
     */
    fun corePassives(): Kit {
        passive(HungerPassiveDefinition)
        passive(RegenerationPassiveDefinition(regenerationRate, maxHealth, regenerationDelay))
        return this
    }

    internal fun build(): KitMetadata {
        require(meleeDamage != 0) { "Melee damage should not be 0" }

        return KitMetadata(
            description = description,
            type = type,
            meleeDamage = meleeDamage,
            passives = passives.toList(),
            abilities = abilities.toList(),
            regenerationRate = regenerationRate,
            maxHealth = maxHealth,
            regenerationDelay = regenerationDelay
        )
    }
}

fun kit(block: Kit.() -> Unit): KitMetadata {
    return Kit().apply(block).build()
}
