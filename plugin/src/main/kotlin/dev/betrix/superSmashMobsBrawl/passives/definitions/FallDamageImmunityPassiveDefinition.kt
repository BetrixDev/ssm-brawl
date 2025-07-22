package dev.betrix.superSmashMobsBrawl.passives.definitions

import dev.betrix.superSmashMobsBrawl.passives.instances.FallDamageImmunityInstance
import dev.betrix.superSmashMobsBrawl.passives.instances.PassiveInstance
import dev.betrix.superSmashMobsBrawl.passives.passive
import org.bukkit.entity.Player

object FallDamageImmunityPassiveDefinition : PassiveDefinition() {
    override val name = "Fall Damage Immunity"
    override val id = "fall_damage_immunity"

    override val metadata = passive {
        description = "Immune to fall damage and creates small explosion on hard landings"
        userFacing = true
    }

    override fun createInstance(player: Player): FallDamageImmunityInstance {
        return FallDamageImmunityInstance(this, player)
    }
}