package dev.betrix.superSmashMobsBrawl.abilities

import org.bukkit.entity.Player

class IronHookAbility(player: Player) : BrawlAbility("iron_hook", player) {

    override fun activate() {
        super.activate()

        // TODO: Implement iron hook projectile
        // This should launch a tripwire hook looking projectile
        // that can pull enemies or create some other effect
    }
}
