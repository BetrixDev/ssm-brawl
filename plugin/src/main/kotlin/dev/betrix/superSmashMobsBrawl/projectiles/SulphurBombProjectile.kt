package dev.betrix.superSmashMobsBrawl.projectiles

import kotlin.math.min
import org.bukkit.Material
import org.bukkit.Particle
import org.bukkit.Sound
import org.bukkit.block.Block
import org.bukkit.entity.LivingEntity
import org.bukkit.entity.Player
import org.bukkit.entity.Projectile
import org.bukkit.entity.ThrownPotion
import org.bukkit.inventory.ItemStack

class SulphurBombProjectile(
    override val owner: Player,
    private val onHit: (entity: LivingEntity?, projectile: Projectile?) -> Boolean,
) : BrawlProjectile(owner, "abilities.sulphur_bomb.name") {
    override var projectileSize = 0.65

    private val projectileVelocityMultiplier = 1.55

    override fun createProjectileEntity(): Projectile {
        return owner.world.spawn(owner.eyeLocation, ThrownPotion::class.java).apply {
            shooter = owner
            item = ItemStack.of(Material.COAL)
        }
    }

    override fun doVelocity() {
        projectile?.velocity = owner.eyeLocation.direction.multiply(projectileVelocityMultiplier)
    }

    override fun onHitLivingEntity(entity: LivingEntity): Boolean {
        return onHit(entity, projectile).let {
            if (it) {
                onHitEffects()
            }

            it
        }
    }

    override fun onHitBlock(block: Block): Boolean {
        return onHit(null, projectile).let {
            if (it) {
                onHitEffects()
            }

            it
        }
    }

    override fun doEffect() {
        val projectile = projectile ?: return

        // Make particles less early on so it doesn't block the shooter's screen
        val particleCount = min(projectile.ticksLived, 8)

        Particle.SMOKE.builder()
            .location(projectile.location)
            .count(particleCount)
            .offset(0.1, 0.1, 0.1)
            .receivers(96, true)
            .extra(0.0)
            .spawn()
    }

    private fun onHitEffects() {
        val projectile = projectile ?: return

        Particle.EXPLOSION.builder()
            .count(1)
            .offset(0.5, 0.5, 0.5)
            .location(projectile.location)
            .receivers(96, true)
            .extra(0.0)
            .spawn()

        projectile.world.playSound(projectile.location, Sound.ENTITY_GENERIC_EXPLODE, 1F, 1F)
    }
}
