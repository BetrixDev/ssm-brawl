package dev.betrix.superSmashMobsBrawl.kits.definitions

import dev.betrix.superSmashMobsBrawl.abilities.definitions.ExplosionAbilityDefinition
import dev.betrix.superSmashMobsBrawl.abilities.definitions.LightningJumpAbilityDefinition
import dev.betrix.superSmashMobsBrawl.abilities.definitions.StealthAbilityDefinition
import dev.betrix.superSmashMobsBrawl.abilities.definitions.SulphurBombAbilityDefinition
import dev.betrix.superSmashMobsBrawl.kits.instances.CreeperKitInstance
import dev.betrix.superSmashMobsBrawl.kits.instances.KitInstance
import dev.betrix.superSmashMobsBrawl.kits.kit
import dev.betrix.superSmashMobsBrawl.passives.definitions.DoubleJumpPassiveDefinition
import dev.betrix.superSmashMobsBrawl.passives.definitions.ExplosiveFeedbackPassiveDefinition
import dev.betrix.superSmashMobsBrawl.passives.definitions.FallDamageImmunityPassiveDefinition
import org.bukkit.entity.Player

object CreeperKitDefinition : KitDefinition() {
    override val name = "Creeper"
    override val id = "creeper"

    override val metadata = kit {
        description = "Explosive assassin with stealth and teleportation abilities"
        meleeDamage = 6

        // Passive abilities
        passive(DoubleJumpPassiveDefinition)
        passive(ExplosiveFeedbackPassiveDefinition)
        passive(FallDamageImmunityPassiveDefinition)

        // Active abilities
        ability(LightningJumpAbilityDefinition)
        ability(SulphurBombAbilityDefinition)
        ability(ExplosionAbilityDefinition)
        ability(StealthAbilityDefinition)
    }

    override fun createInstance(player: Player): CreeperKitInstance {
        return CreeperKitInstance(this, player)
    }
}