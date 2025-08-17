package dev.betrix.superSmashMobsBrawl.projectiles

import dev.betrix.superSmashMobsBrawl.events.Damager
import dev.betrix.superSmashMobsBrawl.events.SmashDamageEvent
import dev.betrix.superSmashMobsBrawl.extensions.setVelocity
import org.bukkit.block.Block
import org.bukkit.block.data.BlockData
import org.bukkit.entity.Entity
import org.bukkit.entity.FallingBlock
import org.bukkit.entity.LivingEntity
import org.bukkit.entity.Player
import org.bukkit.entity.Projectile

class BlockProjectile(
    override val owner: Player,
    private val charge: Long,
    private val multiplier: Double,
    private val blockData: BlockData
) :
    BrawlProjectile(owner, "block") {

    private val damage = 8.0
    private val maxDamage = 9.0
    private val kockbackMultplier = 2.5
    override var projectileSize = 0.75

    override fun createProjectileEntity(): Entity {
        return owner.world.spawnFallingBlock(owner.eyeLocation.add(owner.location.direction), blockData)
    }

    override fun doVelocity() {
        val projectile = projectile ?: return

        projectile.setVelocity(owner.location.direction, multiplier, false, 0.2, 0.0, 1.0, true)
    }

    override fun onHitLivingEntity(entity: LivingEntity): Boolean {
        val blockDamage = Math.min(maxDamage, projectile?.velocity?.length()?.times(damage) ?: maxDamage)

        SmashDamageEvent(entity, Damager.DamagerLivingEntity(owner), blockDamage).apply {
            knockbackMultiplier *= kockbackMultplier
        }.callEvent()

        try {
            (projectile as? FallingBlock)?.world?.spawnFallingBlock(
                projectile?.location!!,
                (projectile as? FallingBlock)?.blockData!!
            )
        } catch (e: Exception) {
            e.printStackTrace()
        }

        return true
    }

    override fun onHitBlock(block: Block): Boolean {
        return true
    }
}