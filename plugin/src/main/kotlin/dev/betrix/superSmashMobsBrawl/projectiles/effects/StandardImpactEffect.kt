package dev.betrix.superSmashMobsBrawl.projectiles.effects

import dev.betrix.superSmashMobsBrawl.projectiles.BrawlProjectile
import dev.betrix.superSmashMobsBrawl.projectiles.HitType
import dev.betrix.superSmashMobsBrawl.projectiles.ProjectileEffect
import org.bukkit.Particle
import org.bukkit.Sound

class StandardImpactEffect(
    private val particle: Particle,
    private val sound: Sound?,
    private val count: Int,
) : ProjectileEffect {

    override fun onTick(projectile: BrawlProjectile) {
        // No tick behavior for impact effects
    }

    override fun onHit(projectile: BrawlProjectile, hitType: HitType) {
        val entity = projectile.projectileEntity ?: return

        particle
            .builder()
            .count(count)
            .offset(0.5, 0.5, 0.5)
            .location(entity.location)
            .receivers(96, true)
            .extra(0.0)
            .spawn()

        sound?.let { entity.world.playSound(entity.location, it, 1F, 1F) }
    }
}
