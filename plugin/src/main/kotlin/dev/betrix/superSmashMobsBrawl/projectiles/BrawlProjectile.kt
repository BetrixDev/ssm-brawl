package dev.betrix.superSmashMobsBrawl.projectiles

import dev.betrix.superSmashMobsBrawl.Manageable
import dev.betrix.superSmashMobsBrawl.extensions.disguise
import dev.betrix.superSmashMobsBrawl.extensions.isAirOrFoliage
import dev.betrix.superSmashMobsBrawl.extensions.setVelocity
import dev.betrix.superSmashMobsBrawl.services.LangService
import gg.flyte.twilight.event.event
import gg.flyte.twilight.scheduler.TwilightRunnable
import gg.flyte.twilight.scheduler.repeatingTask
import kotlin.math.min
import org.bukkit.*
import org.bukkit.block.Block
import org.bukkit.block.BlockFace
import org.bukkit.entity.*
import org.bukkit.event.entity.ProjectileHitEvent
import org.bukkit.inventory.ItemStack
import org.bukkit.util.BoundingBox
import org.bukkit.util.RayTraceResult
import org.bukkit.util.Vector
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

// Callback type aliases
typealias EntityHitCallback =
    (entity: LivingEntity, projectile: BrawlProjectile) -> ProjectileAction

typealias BlockHitCallback = (block: Block, projectile: BrawlProjectile) -> ProjectileAction

typealias ExpireCallback = (projectile: BrawlProjectile) -> ProjectileAction

typealias IdleCallback = (projectile: BrawlProjectile) -> ProjectileAction

typealias TickCallback = (projectile: BrawlProjectile) -> Unit

enum class ProjectileAction {
    CONTINUE, // Keep projectile alive
    DESTROY, // Destroy projectile
    BOUNCE, // Bounce off surface (for block hits)
}

enum class HitType {
    ENTITY,
    BLOCK,
    EXPIRE,
    IDLE,
}

// Effect system
interface ProjectileEffect {
    fun onTick(projectile: BrawlProjectile)

    fun onHit(projectile: BrawlProjectile, hitType: HitType)
}

abstract class BrawlProjectile(open val owner: Player, open val name: String) :
    Manageable(), KoinComponent {
    private val lang: LangService by inject()
    private var job: TwilightRunnable? = null

    @Volatile
    var projectileEntity: Entity? = null
        protected set

    // Configuration properties
    protected var maxLifetimeTicks = 20L * 30L // 30 seconds in ticks
    protected var doEntityDetection = true
    protected var doBlockDetection = true
    protected var doIdleDetection = true
    protected var projectileSize = 0.5
    protected open var velocityMultiplier = 1.0
    protected var velocityAddend: Vector? = null

    // Callback collections
    private val entityHitCallbacks = mutableListOf<EntityHitCallback>()
    private val blockHitCallbacks = mutableListOf<BlockHitCallback>()
    private val expireCallbacks = mutableListOf<ExpireCallback>()
    private val idleCallbacks = mutableListOf<IdleCallback>()
    private val tickCallbacks = mutableListOf<TickCallback>()

    // Effects
    private val effects = mutableListOf<ProjectileEffect>()

    var velocityBeforeImpact = Vector()
        private set

    private var cachedBoundingBox: BoundingBox? = null
    private var lastProjectileLocation: Location? = null

    val projectileBoundingBox: BoundingBox
        get() {
            val projectile = projectileEntity ?: return BoundingBox(0.0, 0.0, 0.0, 0.0, 0.0, 0.0)

            val currentLocation = projectile.location
            if (cachedBoundingBox != null && lastProjectileLocation == currentLocation) {
                return cachedBoundingBox!!
            }

            val location = projectile.location
            val centerX = location.x
            val centerY = location.y + (projectileSize / 2.0)
            val centerZ = location.z

            val halfSize = projectileSize

            val boundingBox =
                BoundingBox.of(
                    location.toVector().setX(centerX).setY(centerY).setZ(centerZ),
                    halfSize,
                    halfSize,
                    halfSize,
                )

            cachedBoundingBox = boundingBox
            lastProjectileLocation = currentLocation.clone()

            return boundingBox
        }

    // Fluent configuration API
    fun maxLifetime(ticks: Long): BrawlProjectile = apply { maxLifetimeTicks = ticks }

    fun projectileSize(size: Double): BrawlProjectile = apply { projectileSize = size }

    fun velocityMultiplier(multiplier: Double): BrawlProjectile = apply {
        velocityMultiplier = multiplier
    }

    fun velocityAddend(vector: Vector?): BrawlProjectile = apply { velocityAddend = vector }

    // Detection toggles
    fun enableEntityDetection(enabled: Boolean = true): BrawlProjectile = apply {
        doEntityDetection = enabled
    }

    fun enableBlockDetection(enabled: Boolean = true): BrawlProjectile = apply {
        doBlockDetection = enabled
    }

    fun enableIdleDetection(enabled: Boolean = true): BrawlProjectile = apply {
        doIdleDetection = enabled
    }

    // Event handlers
    fun onHitEntity(callback: EntityHitCallback): BrawlProjectile = apply {
        entityHitCallbacks.add(callback)
    }

    fun onHitBlock(callback: BlockHitCallback): BrawlProjectile = apply {
        blockHitCallbacks.add(callback)
    }

    fun onExpire(callback: ExpireCallback): BrawlProjectile = apply {
        expireCallbacks.add(callback)
    }

    fun onIdle(callback: IdleCallback): BrawlProjectile = apply { idleCallbacks.add(callback) }

    fun onTick(callback: TickCallback): BrawlProjectile = apply { tickCallbacks.add(callback) }

    // Effect system
    fun addEffect(effect: ProjectileEffect): BrawlProjectile = apply { effects.add(effect) }

    fun trailEffect(particle: Particle, count: Int = 1, offset: Vector? = null): BrawlProjectile =
        apply {
            addEffect(TrailEffect(particle, count, offset ?: Vector(0.1, 0.1, 0.1)))
        }

    fun impactEffect(particle: Particle, sound: Sound? = null, count: Int = 1): BrawlProjectile =
        apply {
            addEffect(ImpactEffect(particle, sound, count))
        }

    fun launch(): BrawlProjectile {
        projectileEntity = createProjectileEntity()
        projectileEntity?.isCustomNameVisible = false
        projectileEntity?.isPersistent = false

        if (projectileEntity is Item) {
            val item = projectileEntity as Item
            item.pickupDelay = 1000000
        }

        doVelocity()

        listeners.add(
            event<ProjectileHitEvent> {
                if (entity == projectileEntity) {
                    isCancelled = true
                }
            }
        )

        job =
            repeatingTask(1) {
                if (
                    projectileEntity == null ||
                        !projectileEntity!!.isValid ||
                        projectileEntity!!.world != owner.world
                ) {
                    teardown()
                    return@repeatingTask
                }

                if (projectileEntity!!.ticksLived > maxLifetimeTicks) {
                    val action = handleExpire()
                    if (action == ProjectileAction.DESTROY) {
                        teardown()
                        return@repeatingTask
                    }
                }

                velocityBeforeImpact = projectileEntity!!.velocity.clone()

                if (doEntityDetection) {
                    checkHitLivingEntity()?.let { entity ->
                        val action = handleEntityHit(entity)
                        if (action == ProjectileAction.DESTROY) {
                            teardown()
                            return@repeatingTask
                        }
                    }
                }

                if (doBlockDetection) {
                    checkHitBlock()?.let { block ->
                        val action = handleBlockHit(block)
                        when (action) {
                            ProjectileAction.DESTROY -> {
                                teardown()
                                return@repeatingTask
                            }

                            ProjectileAction.BOUNCE -> {
                                handleBounce(block)
                            }

                            ProjectileAction.CONTINUE -> {
                                // Continue as normal
                            }
                        }
                    }
                }

                if (doIdleDetection) {
                    if (checkIdle()) {
                        val action = handleIdle()
                        if (action == ProjectileAction.DESTROY) {
                            teardown()
                            return@repeatingTask
                        }
                    }
                }

                // Execute tick callbacks
                tickCallbacks.forEach { it(this@BrawlProjectile) }

                // Execute effects
                effects.forEach { it.onTick(this@BrawlProjectile) }
            }

        return this
    }

    abstract fun createProjectileEntity(): Entity

    open fun doVelocity() {
        val baseVelocity = owner.eyeLocation.direction
        val finalVelocity =
            if (velocityAddend != null) {
                baseVelocity.add(velocityAddend!!).multiply(velocityMultiplier)
            } else {
                baseVelocity.multiply(velocityMultiplier)
            }
        projectileEntity?.velocity = finalVelocity
    }

    private fun handleEntityHit(entity: LivingEntity): ProjectileAction {
        effects.forEach { it.onHit(this, HitType.ENTITY) }

        return if (entityHitCallbacks.isEmpty()) {
            onHitLivingEntity(entity)
        } else {
            var finalAction = ProjectileAction.CONTINUE
            entityHitCallbacks.forEach { callback ->
                val action = callback(entity, this)
                if (action == ProjectileAction.DESTROY) {
                    finalAction = ProjectileAction.DESTROY
                }
            }
            finalAction
        }
    }

    private fun handleBlockHit(block: Block): ProjectileAction {
        effects.forEach { it.onHit(this, HitType.BLOCK) }

        return if (blockHitCallbacks.isEmpty()) {
            if (onHitBlock(block)) ProjectileAction.DESTROY else ProjectileAction.CONTINUE
        } else {
            var finalAction = ProjectileAction.CONTINUE
            blockHitCallbacks.forEach { callback ->
                val action = callback(block, this)
                if (action == ProjectileAction.DESTROY) {
                    finalAction = ProjectileAction.DESTROY
                } else if (
                    action == ProjectileAction.BOUNCE && finalAction != ProjectileAction.DESTROY
                ) {
                    finalAction = ProjectileAction.BOUNCE
                }
            }
            finalAction
        }
    }

    private fun handleExpire(): ProjectileAction {
        effects.forEach { it.onHit(this, HitType.EXPIRE) }

        return if (expireCallbacks.isEmpty()) {
            if (onExpire()) ProjectileAction.DESTROY else ProjectileAction.CONTINUE
        } else {
            var finalAction = ProjectileAction.CONTINUE
            expireCallbacks.forEach { callback ->
                val action = callback(this)
                if (action == ProjectileAction.DESTROY) {
                    finalAction = ProjectileAction.DESTROY
                }
            }
            finalAction
        }
    }

    private fun handleIdle(): ProjectileAction {
        effects.forEach { it.onHit(this, HitType.IDLE) }

        return if (idleCallbacks.isEmpty()) {
            if (onIdle()) ProjectileAction.DESTROY else ProjectileAction.CONTINUE
        } else {
            var finalAction = ProjectileAction.CONTINUE
            idleCallbacks.forEach { callback ->
                val action = callback(this)
                if (action == ProjectileAction.DESTROY) {
                    finalAction = ProjectileAction.DESTROY
                }
            }
            finalAction
        }
    }

    private fun handleBounce(block: Block) {
        val projectile = projectileEntity ?: return
        val velocity = projectile.velocity

        // Simple bounce logic - reverse the appropriate velocity component
        val normal = getBlockNormal(block, projectile.location)
        val bounceVelocity = velocity.clone().subtract(normal.multiply(2 * velocity.dot(normal)))

        projectile.velocity = bounceVelocity.multiply(0.8) // Reduce velocity on bounce
    }

    private fun getBlockNormal(block: Block, projectileLocation: Location): Vector {
        val blockCenter = block.location.add(0.5, 0.5, 0.5)
        val direction = projectileLocation.toVector().subtract(blockCenter.toVector()).normalize()

        // Determine which face was hit based on direction
        return when {
            kotlin.math.abs(direction.x) > kotlin.math.abs(direction.y) &&
                kotlin.math.abs(direction.x) > kotlin.math.abs(direction.z) -> {
                Vector(if (direction.x > 0) 1.0 else -1.0, 0.0, 0.0)
            }

            kotlin.math.abs(direction.y) > kotlin.math.abs(direction.z) -> {
                Vector(0.0, if (direction.y > 0) 1.0 else -1.0, 0.0)
            }

            else -> {
                Vector(0.0, 0.0, if (direction.z > 0) 1.0 else -1.0)
            }
        }
    }

    // Default implementations (can be overridden for backward compatibility)
    open fun onHitLivingEntity(entity: LivingEntity): ProjectileAction = ProjectileAction.DESTROY

    open fun onHitBlock(block: Block): Boolean = true

    open fun onExpire(): Boolean = true

    open fun onIdle(): Boolean = true

    override fun teardown() {
        job?.cancel()
        projectileEntity?.remove()
    }

    private fun checkHitLivingEntity(): LivingEntity? {
        val projectile = projectileEntity ?: return null

        val possibleLivingEntities =
            projectile.world.livingEntities.filter { entity ->
                if (entity == owner) {
                    return@filter false
                }

                val candidateBox = (entity as? Player)?.disguise?.boundingBox ?: entity.boundingBox
                return@filter candidateBox.overlaps(projectileBoundingBox)
            }

        return possibleLivingEntities.minByOrNull { it.location.distance(projectile.location) }
    }

    private fun checkHitBlock(): Block? {
        val projectile = projectileEntity ?: return null
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
        val projectile = projectileEntity ?: return true

        if (projectile.isDead || !projectile.isValid) {
            return true
        }

        val checkBlock = projectile.location.block.getRelative(BlockFace.DOWN)

        return projectile.velocity.length() <= 0.01 &&
            (projectile.isOnGround || checkBlock.isAirOrFoliage())
    }

    companion object {
        fun arrow(
            owner: Player,
            velocityMultiplier: Double = 1.0,
            name: String = "messages.projectiles.arrow",
        ): ArrowProjectile = ArrowProjectile(owner, velocityMultiplier, name)

        fun potion(
            owner: Player,
            item: ItemStack,
            name: String = "messages.projectiles.potion",
        ): PotionProjectile = PotionProjectile(owner, item, name)

        fun custom(owner: Player, name: String, entitySupplier: () -> Entity): CustomProjectile =
            CustomProjectile(owner, name, entitySupplier)
    }
}

// Concrete implementations
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

class PotionProjectile(
    owner: Player,
    private val item: ItemStack,
    name: String = "messages.projectiles.potion",
) : BrawlProjectile(owner, name) {

    init {
        projectileSize(0.65)
    }

    override fun createProjectileEntity(): Projectile =
        owner.world.spawn(owner.eyeLocation, ThrownPotion::class.java).apply {
            shooter = owner
            this.item = this@PotionProjectile.item
        }
}

class CustomProjectile(owner: Player, name: String, private val entitySupplier: () -> Entity) :
    BrawlProjectile(owner, name) {

    override fun createProjectileEntity(): Entity = entitySupplier()

    override fun doVelocity() {
        projectileEntity?.setVelocity(
            owner.location.direction,
            velocityMultiplier,
            false,
            0.2,
            0.0,
            1.0,
            true,
        )
    }
}

// Effect implementations
class TrailEffect(
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

class ImpactEffect(
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
