package dev.betrix.superSmashMobsBrawl.projectiles

import gg.flyte.twilight.extension.add
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

        projectile.world.spawnParticle(
            Particle.SMOKE,
            projectile.location,
            particleCount,
            0.1,
            0.1,
            0.1,
            0.01,
        )

        projectile.world.spawnParticle(
            Particle.FIREFLY,
            projectile.location,
            1,
            0.1,
            0.1,
            0.1,
            0.01,
        )
    }

    private fun onHitEffects() {
        val projectile = projectile ?: return

        projectile.world.spawnParticle(Particle.EXPLOSION, projectile.location.add(0, 0.25, 0), 1)
        projectile.world.playSound(projectile.location, Sound.ENTITY_GENERIC_EXPLODE, 1F, 1F)
    }
}
