package dev.betrix.superSmashMobsBrawl.kits.definitions

import dev.betrix.superSmashMobsBrawl.kits.instances.KitInstance
import dev.betrix.superSmashMobsBrawl.kits.kit
import org.bukkit.entity.Player

object CreeperKitDefinition : KitDefinition() {
    override val name = "Creeper"
    override val id = "creeper"

    override val metadata = kit {
        description = "OG kit"

        passive("double_jump")

        ability("sulphur_bomb")
    }

    override fun createInstance(player: Player): KitInstance {
        TODO("Not yet implemented")
    }
}