package dev.betrix.superSmashMobsBrawl.projectiles

import dev.betrix.superSmashMobsBrawl.IManageable
import dev.betrix.superSmashMobsBrawl.extensions.getDisguise
import dev.betrix.superSmashMobsBrawl.extensions.isAirOrFoliage
import dev.betrix.superSmashMobsBrawl.utils.mm
import gg.flyte.twilight.event.event
import gg.flyte.twilight.scheduler.TwilightRunnable
import gg.flyte.twilight.scheduler.repeatingTask
import org.bukkit.FluidCollisionMode
import org.bukkit.block.Block
import org.bukkit.block.BlockFace
import org.bukkit.entity.Item
import org.bukkit.entity.LivingEntity
import org.bukkit.entity.Player
import org.bukkit.entity.Projectile
import org.bukkit.event.entity.ProjectileHitEvent
import org.bukkit.util.BoundingBox
import org.bukkit.util.RayTraceResult
import org.bukkit.util.Vector

abstract class BrawlProjectile(open val owner: Player, open val name: String) : IManageable {
    private var job: TwilightRunnable? = null

    protected var projectile: Projectile? = null
    protected var maxLifetimeTicks = 20L * 30L // 30 seconds in ticks
    protected open var doEntityDetection = true
    protected open var doBlockDetection = true
    protected open var doIdleDetection = true
    protected open var projectileSize = 0.5

    var velocityBeforeImpact = Vector()
        private set

    val projectileBoundingBox: BoundingBox
        get() {
            val projectile = projectile ?: return BoundingBox(0.0, 0.0, 0.0, 0.0, 0.0, 0.0)

            val location = projectile.location
            val centerX = location.x
            val centerY = location.y + (projectileSize / 2.0)
            val centerZ = location.z

            val halfSize = projectileSize

            return BoundingBox.of(
                location.toVector().setX(centerX).setY(centerY).setZ(centerZ),
                halfSize,
                halfSize,
                halfSize,
            )
        }

    fun launch() {
        projectile = createProjectileEntity()
        projectile?.customName(mm(name))

        if (projectile is Item) {
            val item = projectile as Item
            item.pickupDelay = 1000000
        }

        doVelocity()

        event<ProjectileHitEvent> {
            if (entity == projectile) {
                isCancelled = true
            }
        }

        job =
            repeatingTask(1) {
                if (
                    projectile == null || !projectile!!.isValid || projectile!!.world != owner.world
                ) {
                    teardown()
                    return@repeatingTask
                }

                if (projectile!!.ticksLived > maxLifetimeTicks && onExpire()) {
                    teardown()
                    return@repeatingTask
                }

                velocityBeforeImpact = projectile!!.velocity.clone()

                if (doEntityDetection) {
                    checkHitLivingEntity()?.let { entity ->
                        if (onHitLivingEntity(entity)) {
                            teardown()
                            return@repeatingTask
                        }
                    }
                }

                if (doBlockDetection) {
                    checkHitBlock()?.let { block ->
                        if (onHitBlock(block)) {
                            teardown()
                            return@repeatingTask
                        }
                    }
                }

                if (doIdleDetection) {
                    if (checkIdle() && onIdle()) {
                        teardown()
                        return@repeatingTask
                    }
                }
                doEffect()
            }
    }

    abstract fun createProjectileEntity(): Projectile

    abstract fun doVelocity()

    open fun doEffect() {}

    open fun onExpire(): Boolean {
        return true
    }

    abstract fun onHitLivingEntity(entity: LivingEntity): Boolean

    abstract fun onHitBlock(block: Block): Boolean

    open fun onIdle(): Boolean {
        return true
    }

    override fun teardown() {
        job?.cancel()
        projectile?.remove()
    }

    private fun checkHitLivingEntity(): LivingEntity? {
        val projectile = projectile ?: return null

        val possibleLivingEntities =
            projectile.world.livingEntities
                .filter { entity ->
                    if (entity == owner) {
                        return@filter false
                    }

                    if (entity is Player) {
                        entity.getDisguise()?.let { disguise ->
                            return@filter disguise.boundingBox.overlaps(projectileBoundingBox)
                        }
                    }

                    return@filter entity.boundingBox.overlaps(projectileBoundingBox)
                }
                .sortedBy { it.location.distance(projectile.location) }

        if (possibleLivingEntities.isEmpty()) {
            return null
        }

        return possibleLivingEntities[0]
    }

    private fun checkHitBlock(): Block? {
        val projectile = projectile ?: return null

        val world = projectile.world
        val currentLocation = projectile.location
        val velocity = projectile.velocity

        if (velocity.length() <= 0.0) {
            return null
        }

        val rayTraceResult: RayTraceResult? =
            world.rayTraceBlocks(
                currentLocation,
                velocity,
                velocity.length(),
                FluidCollisionMode.NEVER,
                true,
            )

        val hitBlock = rayTraceResult?.hitBlock ?: return null

        if (hitBlock.isLiquid || !hitBlock.type.isSolid) {
            return null
        }

        val hitPoint = rayTraceResult.hitPosition
        val currentPos = currentLocation.toVector()
        val newMotion = hitPoint.subtract(currentPos)

        projectile.velocity = newMotion

        val magnitude = newMotion.length()
        if (magnitude > 0) {
            val normalizedMotion = newMotion.clone().multiply(1.0 / magnitude)
            val adjustedPosition = currentLocation.subtract(normalizedMotion.multiply(0.05))
            projectile.teleport(adjustedPosition)
        }

        return hitBlock
    }

    private fun checkIdle(): Boolean {
        val projectile = projectile ?: return true

        if (projectile.isDead || !projectile.isValid) {
            return true
        }

        val checkBlock = projectile.location.block.getRelative(BlockFace.DOWN)

        return projectile.velocity.length() < 0 &&
            (projectile.isOnGround || checkBlock.isAirOrFoliage())
    }
}
