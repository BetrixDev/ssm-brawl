package dev.betrix.superSmashMobsBrawl.projectiles.effects

import dev.betrix.superSmashMobsBrawl.projectiles.BrawlProjectile
import dev.betrix.superSmashMobsBrawl.projectiles.HitType
import dev.betrix.superSmashMobsBrawl.projectiles.ProjectileEffect
import kotlin.math.min
import org.bukkit.Particle
import org.bukkit.util.Vector

class StandardTrailEffect(
    private val particle: Particle,
    private val count: Int,
    private val offset: Vector,
) : ProjectileEffect {

    override fun onTick(projectile: BrawlProjectile) {
        val entity = projectile.projectileEntity ?: return

        // Make particles less early on so it doesn't block the shooter's screen
        val particleCount = min(entity.ticksLived, count)

        particle
            .builder()
            .location(entity.location)
            .count(particleCount)
            .offset(offset.x, offset.y, offset.z)
            .receivers(96, true)
            .extra(0.0)
            .spawn()
    }

    override fun onHit(projectile: BrawlProjectile, hitType: HitType) {
        // No special hit behavior for trail effects
    }
}
