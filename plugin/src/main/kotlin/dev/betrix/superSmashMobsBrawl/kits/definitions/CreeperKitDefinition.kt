package dev.betrix.superSmashMobsBrawl.kits.definitions

import dev.betrix.superSmashMobsBrawl.abilities.definitions.SulphurBombAbilityDefinition
import dev.betrix.superSmashMobsBrawl.kits.instances.CreeperKitInstance
import dev.betrix.superSmashMobsBrawl.kits.instances.KitInstance
import dev.betrix.superSmashMobsBrawl.kits.kit
import dev.betrix.superSmashMobsBrawl.passives.definitions.DoubleJumpPassiveDefinition
import org.bukkit.entity.Player

object CreeperKitDefinition : KitDefinition() {
    override val name = "Creeper"
    override val id = "creeper"

    override val metadata = kit {
        description = "OG kit"

        passive(DoubleJumpPassiveDefinition)

        ability(SulphurBombAbilityDefinition)
    }

    override fun createInstance(player: Player): KitInstance {
        return CreeperKitInstance(this, player)
    }
}