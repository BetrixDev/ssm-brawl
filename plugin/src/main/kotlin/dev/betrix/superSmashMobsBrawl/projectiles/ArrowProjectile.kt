package dev.betrix.superSmashMobsBrawl.projectiles

import org.bukkit.entity.Arrow
import org.bukkit.entity.Player
import org.bukkit.entity.Projectile

class ArrowProjectile(
    owner: Player,
    velocityMultiplier: Double = 1.0,
    name: String = "messages.projectiles.arrow",
) : BrawlProjectile(owner, name) {

    init {
        velocityMultiplier(velocityMultiplier)
        projectileSize(0.5)
    }

    override fun createProjectileEntity(): Projectile =
        owner.world.spawn(owner.eyeLocation, Arrow::class.java).apply { shooter = owner }
}
