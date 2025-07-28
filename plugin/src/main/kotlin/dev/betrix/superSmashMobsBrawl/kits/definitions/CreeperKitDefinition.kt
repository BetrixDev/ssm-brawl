package dev.betrix.superSmashMobsBrawl.kits.definitions

import dev.betrix.superSmashMobsBrawl.abilities.definitions.ExplosionAbilityDefinition
import dev.betrix.superSmashMobsBrawl.abilities.definitions.SulphurBombAbilityDefinition
import dev.betrix.superSmashMobsBrawl.kits.KitValues
import dev.betrix.superSmashMobsBrawl.kits.instances.CreeperKitInstance
import dev.betrix.superSmashMobsBrawl.kits.kit
import dev.betrix.superSmashMobsBrawl.passives.definitions.DoubleJumpPassiveDefinition
import dev.betrix.superSmashMobsBrawl.passives.definitions.ExplosiveFeedbackPassiveDefinition
import dev.betrix.superSmashMobsBrawl.passives.definitions.HungerPassiveDefinition
import dev.betrix.superSmashMobsBrawl.passives.definitions.RegenerationPassiveDefinition
import org.bukkit.entity.Player

object CreeperKitDefinition : KitDefinition() {
    override val name = "Creeper"
    override val id = "creeper"

    override val metadata = kit {
        description = "Explosive assassin with stealth and teleportation abilities"
        meleeDamage = KitValues.CREEPER.MELEE_DAMAGE
        
        // Regeneration settings (based on Mineplex SSM values)
        regenerationRate = KitValues.CREEPER.REGENERATION_RATE
        maxHealth = KitValues.CREEPER.MAX_HEALTH
        regenerationDelay = KitValues.CREEPER.REGENERATION_DELAY

        // Core passive abilities for all SSMB kits
        corePassives()
        
        // Kit-specific passive abilities
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
