package dev.betrix.superSmashMobsBrawl.kits

import dev.betrix.superSmashMobsBrawl.abilities.definitions.AbilityDefinition
import dev.betrix.superSmashMobsBrawl.passives.definitions.PassiveDefinition

data class KitMetadata(
    val description: String = "",
    val type: KitType = KitType.DEFAULT,
    val meleeDamage: Int = 0,
    val passives: List<PassiveDefinition>,
    val abilities: List<AbilityDefinition>
)

enum class KitType { DEFAULT }

class Kit {
    var description = ""
    var type = KitType.DEFAULT
    var meleeDamage = 0
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

    internal fun build(): KitMetadata {
        require(meleeDamage != 0) { "Melee damage should not be 0" }

        return KitMetadata(
            description = description,
            type = type,
            meleeDamage = meleeDamage,
            passives = passives.toList(),
            abilities = abilities.toList(),
        )
    }
}

fun kit(block: Kit.() -> Unit): KitMetadata {
    return Kit().apply(block).build()
}