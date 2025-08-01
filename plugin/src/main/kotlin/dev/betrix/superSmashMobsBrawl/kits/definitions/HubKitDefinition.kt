package dev.betrix.superSmashMobsBrawl.kits.definitions

import dev.betrix.superSmashMobsBrawl.kits.instances.HubKitInstance
import dev.betrix.superSmashMobsBrawl.kits.kit
import dev.betrix.superSmashMobsBrawl.passives.definitions.DoubleJumpPassiveDefinition
import org.bukkit.entity.Player

object HubKitDefinition : KitDefinition() {
    override val name = "Hub"
    override val id = "hub"

    override val metadata = kit {
        description = "Hub kit with double jump ability"
        meleeDamage = 0 // No melee damage since this is just for hub functionality
        isUserFacing = false // This kit should not be selectable by users

        passive(DoubleJumpPassiveDefinition)
        // No abilities - hub kit only has double jump passive
    }

    override fun createInstance(player: Player): HubKitInstance {
        return HubKitInstance(this, player)
    }
}