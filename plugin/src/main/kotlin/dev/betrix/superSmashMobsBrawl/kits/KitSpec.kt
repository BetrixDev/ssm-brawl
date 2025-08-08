package dev.betrix.superSmashMobsBrawl.kits

import dev.betrix.superSmashMobsBrawl.abilities.AbilitySpec
import dev.betrix.superSmashMobsBrawl.passives.PassiveSpec

/** Runtime specification for a kit, built from data files. */
data class KitSpec(
    val id: String,
    val name: String,
    val description: String,
    val type: KitType,
    val meleeDamage: Int,
    val passives: List<PassiveSpec>,
    val abilities: List<AbilitySpec>,
)
