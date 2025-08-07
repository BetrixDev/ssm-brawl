package dev.betrix.superSmashMobsBrawl.kits.definitions

import dev.betrix.superSmashMobsBrawl.abilities.definitions.ExplosionAbilityDefinition
import dev.betrix.superSmashMobsBrawl.abilities.definitions.SulphurBombAbilityDefinition
import dev.betrix.superSmashMobsBrawl.kits.instances.CreeperKitInstance
import dev.betrix.superSmashMobsBrawl.kits.kit
import org.bukkit.entity.Player

object CreeperKitDefinition : KitDefinition() {
    override val name = "Creeper"
    override val id = "creeper"

    override val metadata = kit {
        description = "He blow up"
        meleeDamage = 6

        passive(dev.betrix.superSmashMobsBrawl.passives.definitions.DoubleJumpPassiveDefinition)
        passive(dev.betrix.superSmashMobsBrawl.passives.definitions.RegenerationPassiveDefinition)
        passive(dev.betrix.superSmashMobsBrawl.passives.definitions.HungerPassiveDefinition)

        ability(SulphurBombAbilityDefinition)
        ability(ExplosionAbilityDefinition)
    }

    override fun createInstance(
        player: Player,
    ): CreeperKitInstance {
        return CreeperKitInstance(this, player)
    }
}
