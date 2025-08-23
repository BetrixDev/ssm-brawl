package dev.betrix.superSmashMobsBrawl.abilities

import dev.betrix.superSmashMobsBrawl.projectiles.BrawlProjectile
import dev.betrix.superSmashMobsBrawl.projectiles.IronHookProjectile
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack

class IronHookAbility(player: Player) : BrawlAbility("iron_hook", player) {
    private val activeProjectiles = mutableListOf<BrawlProjectile>()

    override fun activate() {
        super.activate()
        throwProjectile()
    }

    override fun teardown() {
        activeProjectiles.forEach { it.teardown() }
        activeProjectiles.clear()
        super.teardown()
    }

    private fun throwProjectile() {
        val hookProjectile = IronHookProjectile(player).launch()

        activeProjectiles.add(hookProjectile)
    }
}