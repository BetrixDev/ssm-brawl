package dev.betrix.superSmashMobsBrawl.kits.definitions

import dev.betrix.superSmashMobsBrawl.abilities.definitions.ExplosionAbilityDefinition
import dev.betrix.superSmashMobsBrawl.abilities.definitions.SulphurBombAbilityDefinition
import dev.betrix.superSmashMobsBrawl.kits.instances.CreeperKitInstance
import dev.betrix.superSmashMobsBrawl.kits.kit
import dev.betrix.superSmashMobsBrawl.passives.definitions.DoubleJumpPassiveDefinition
import dev.betrix.superSmashMobsBrawl.passives.definitions.ExplosiveFeedbackPassiveDefinition
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

        // Active abilities
        ability(SulphurBombAbilityDefinition)
        ability(ExplosionAbilityDefinition)
    }

    override fun createInstance(player: Player): CreeperKitInstance {
        return CreeperKitInstance(this, player)
    }
}
