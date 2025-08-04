package dev.betrix.superSmashMobsBrawl.projectiles

import org.bukkit.block.Block
import org.bukkit.entity.Arrow
import org.bukkit.entity.LivingEntity
import org.bukkit.entity.Player
import org.bukkit.entity.Projectile
import org.bukkit.util.Vector

class ArrowProjectile(
    override val owner: Player,
    override val name: String = "Arrow",
    private val velocityMultiplier: Double,
    private val velocityAddend: Vector? = null,
) : BrawlProjectile(owner, name) {
    override var projectileSize = 0.5 // Default arrow hitbox

    private var onHitLivingEntityListener:
        ((entity: LivingEntity, projectile: BrawlProjectile) -> Boolean)? =
        null
    private var onBlockListener: ((block: Block, projectile: BrawlProjectile) -> Boolean)? = null

    override fun createProjectileEntity(): Projectile {
        return owner.world.spawn(owner.eyeLocation, Arrow::class.java).apply { shooter = owner }
    }

    override fun doVelocity() {
        if (velocityAddend != null) {
            projectile?.velocity =
                owner.eyeLocation.direction.add(velocityAddend).multiply(velocityMultiplier)
        } else {
            projectile?.velocity = owner.eyeLocation.direction.multiply(velocityMultiplier)
        }
    }

    fun onHitLivingEntity(
        cb: (entity: LivingEntity, projectile: BrawlProjectile) -> Boolean
    ): ArrowProjectile {
        onHitLivingEntityListener = cb

        return this
    }

    override fun onHitLivingEntity(entity: LivingEntity): Boolean {
        return onHitLivingEntityListener?.invoke(entity, this) ?: false
    }

    fun onHitBlock(cb: (block: Block, projectile: BrawlProjectile) -> Boolean): ArrowProjectile {
        onBlockListener = cb

        return this
    }

    override fun onHitBlock(block: Block): Boolean {
        return onBlockListener?.invoke(block, this) ?: false
    }
}
