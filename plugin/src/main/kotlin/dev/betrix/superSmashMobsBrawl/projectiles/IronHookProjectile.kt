package dev.betrix.superSmashMobsBrawl.projectiles

import org.bukkit.Material
import org.bukkit.entity.Item
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack

class IronHookProjectile(owner: Player, name: String = "messages.projectiles.custom") :
    BrawlProjectile(owner, name) {

    init {
        projectileSize(0.45)
        velocityMultiplier(1.6)
    }

    override fun createProjectileEntity(): Item =
        owner.world.dropItem(owner.eyeLocation, ItemStack.of(Material.TRIPWIRE_HOOK)).apply {}
}
