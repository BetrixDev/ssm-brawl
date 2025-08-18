package dev.betrix.superSmashMobsBrawl.projectiles

import dev.betrix.superSmashMobsBrawl.extensions.setVelocity
import org.bukkit.entity.Entity
import org.bukkit.entity.Player

class CustomProjectile(owner: Player, name: String, private val entitySupplier: () -> Entity) :
    BrawlProjectile(owner, name) {

    override fun createProjectileEntity(): Entity = entitySupplier()

    override fun doVelocity() {
        projectileEntity?.setVelocity(
            owner.eyeLocation.direction,
            velocityMultiplier,
            false,
            0.2,
            0.0,
            1.0,
            true,
        )
    }
}
