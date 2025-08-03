package dev.betrix.superSmashMobsBrawl.projectiles

import org.bukkit.block.Block
import org.bukkit.entity.Arrow
import org.bukkit.entity.LivingEntity
import org.bukkit.entity.Player
import org.bukkit.entity.Projectile

class ArrowProjectile(
    override val owner: Player,
    override val name: String = "Arrow",
    private val velocityMultiplier: Double,
) : BrawlProjectile(owner, name) {
    override var projectileSize = 0.5 // Default arrow hitbox

    private var onHitLivingEntityListener: (ArrowProjectile.(entity: LivingEntity, projectile: Projectile) -> Boolean)? = null
    private var onBlockListener: (ArrowProjectile.(block: Block, projectile: Projectile) -> Boolean)? = null

    override fun createProjectileEntity(): Projectile {
        return owner.world.spawn(owner.eyeLocation, Arrow::class.java).apply { shooter = owner }
    }

    override fun doVelocity() {
        projectile?.velocity = owner.eyeLocation.direction.multiply(velocityMultiplier)
    }

    fun onHitLivingEntity(cb: ArrowProjectile.(entity: LivingEntity, projectile: Projectile) -> Boolean): ArrowProjectile {
        onHitLivingEntityListener = cb

        return this
    }

    override fun onHitLivingEntity(entity: LivingEntity): Boolean {
        return onHitLivingEntityListener?.invoke(this, entity, projectile!!) ?: false
    }

    fun onHitBlock(cb: ArrowProjectile.(block: Block, projectile: Projectile) -> Boolean): ArrowProjectile {
        onBlockListener = cb

        return this
    }

    override fun onHitBlock(block: Block): Boolean {
        return onBlockListener?.invoke(this, block, projectile!!) ?: false
    }
}
