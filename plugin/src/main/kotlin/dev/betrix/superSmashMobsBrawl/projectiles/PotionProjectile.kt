package dev.betrix.superSmashMobsBrawl.projectiles

import org.bukkit.entity.Player
import org.bukkit.entity.Projectile
import org.bukkit.entity.ThrownPotion
import org.bukkit.inventory.ItemStack

class PotionProjectile(
    owner: Player,
    private val item: ItemStack,
    name: String = "messages.projectiles.potion",
) : BrawlProjectile(owner, name) {

    init {
        projectileSize(0.65)
    }

    override fun createProjectileEntity(): Projectile =
        owner.world.spawn(owner.eyeLocation, ThrownPotion::class.java).apply {
            shooter = owner
            this.item = this@PotionProjectile.item.clone()
        }
}
