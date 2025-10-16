package dev.betrix.superSmashMobsBrawl.projectiles

import dev.betrix.superSmashMobsBrawl.events.BrawlDamageEvent
import dev.betrix.superSmashMobsBrawl.events.BrawlDamageType
import dev.betrix.superSmashMobsBrawl.events.Damager
import dev.betrix.superSmashMobsBrawl.extensions.setVelocity
import dev.betrix.superSmashMobsBrawl.services.MinigameService
import gg.flyte.twilight.event.event
import gg.flyte.twilight.scheduler.delay
import gg.flyte.twilight.scheduler.repeatingTask
import org.bukkit.Material
import org.bukkit.entity.Item
import org.bukkit.entity.LivingEntity
import org.bukkit.entity.Player
import org.bukkit.entity.Slime
import org.bukkit.event.entity.EntityTargetEvent
import org.bukkit.event.entity.SlimeSplitEvent
import org.bukkit.inventory.ItemStack
import org.bukkit.util.Vector
import org.koin.core.component.inject

class SlimeProjectile(owner: Player, name: String, private val charge: Double) :
    BrawlProjectile(owner, name) {

    private val minigameService: MinigameService by inject()

    private var lastHitTimeMs = 0L
    private val hitCooldownMs = 500L
    private val baseDamage = 3.0
    private val knockbackMult = 3.0
    private val slimeDecayTicks = 120L

    init {
        // Slimes are larger, so increase hitbox size
        projectileSize(0.75)
        maxLifetime(600L) // 30 seconds total lifetime
    }

    override fun createProjectileEntity(): Slime {
        val slime = owner.world.spawn(owner.eyeLocation, Slime::class.java)
        slime.size = (charge.toInt()).coerceIn(1, 3)
        val maxHealth = 5.0 + charge * 7.0
        slime.getAttribute(org.bukkit.attribute.Attribute.MAX_HEALTH)?.baseValue = maxHealth
        slime.health = maxHealth
        slime.customName(null)
        slime.isCustomNameVisible = false

        // Setup event listeners after entity creation
        setupEventListeners()

        return slime
    }

    override fun doVelocity() {
        val entity = projectileEntity as? Slime ?: return
        val velocityStrength = 1.0 + charge / 2.0

        entity.setVelocity(
            owner.eyeLocation.direction,
            velocityStrength,
            false,
            0.0,
            0.2,
            10.0,
            true,
        )

        // Start size decay task after velocity is set
        startSizeDecayTask()
    }

    private fun setupEventListeners() {
        // Prevent slime from splitting when hit
        listeners.add(
            event<SlimeSplitEvent> {
                if (entity == projectileEntity) {
                    isCancelled = true
                }
            }
        )

        // Prevent slime from targeting owner or allies
        listeners.add(
            event<EntityTargetEvent> {
                if (entity != projectileEntity) return@event

                val target = target as? Player ?: return@event

                // Cancel if targeting owner
                if (target == owner) {
                    isCancelled = true
                    return@event
                }

                // Cancel if on same team (checked by minigame)
                val minigame = minigameService.getMinigameForPlayer(owner)
                if (
                    minigame != null &&
                        minigame.arePlayersOnSameTeam(owner, target)
                ) {
                    isCancelled = true
                }
            }
        )
    }

    private fun startSizeDecayTask() {
        // Size decay over time - slime loses size every 120 ticks (6 seconds)
        runnables.add(
            repeatingTask(20) {
                val slime = projectileEntity as? Slime ?: run {
                    cancel()
                    return@repeatingTask
                }

                if (!slime.isValid) {
                    cancel()
                    return@repeatingTask
                }

                if (slime.ticksLived > slimeDecayTicks) {
                    slime.ticksLived = 1

                    // Create slime particle effect
                    val particleCount = 6 + 6 * slime.size
                    repeat(particleCount) {
                        val item =
                            slime.world.spawn(
                                slime.location
                                    .clone()
                                    .add(
                                        (Math.random() - 0.5) *
                                            0.2 *
                                            (1 + 0.1 * slime.size),
                                        (Math.random() - 0.5) *
                                            0.2 *
                                            (1 + 0.1 * slime.size),
                                        (Math.random() - 0.5) *
                                            0.2 *
                                            (1 + 0.1 * slime.size),
                                    ),
                                Item::class.java,
                            )
                        item.itemStack = ItemStack(Material.SLIME_BALL)
                        item.pickupDelay = Int.MAX_VALUE
                        item.velocity =
                            item.velocity
                                .clone()
                                .add(Vector(Math.random() - 0.5, Math.random(), Math.random() - 0.5))
                        item.velocity = item.velocity.multiply(1f)

                        // Remove item after 15 ticks
                        delay(15) { item.remove() }
                    }

                    // Shrink slime
                    if (slime.size <= 1) {
                        teardown()
                    } else {
                        slime.size = slime.size - 1
                    }
                }
            }
        )
    }

    fun setupHitCallback(): SlimeProjectile {
        onHitEntity { entity, _ ->
            val slime = projectileEntity as? Slime ?: return@onHitEntity ProjectileAction.DESTROY

            // Check hit cooldown
            val currentTime = System.currentTimeMillis()
            if (currentTime - lastHitTimeMs < hitCooldownMs) {
                return@onHitEntity ProjectileAction.CONTINUE
            }

            // Don't hit owner
            if (entity == owner) {
                return@onHitEntity ProjectileAction.CONTINUE
            }

            // Check if on same team
            val minigame = minigameService.getMinigameForPlayer(owner)
            if (entity is Player && minigame != null) {
                if (minigame.arePlayersOnSameTeam(owner, entity)) {
                    return@onHitEntity ProjectileAction.CONTINUE
                }
            }

            val damage = baseDamage + slime.size * 3.0

            BrawlDamageEvent(
                    entity,
                    Damager.DamagerLivingEntity(owner),
                    damage,
                    knockbackMult,
                    BrawlDamageType.Projectile,
                )
                .callEvent()

            lastHitTimeMs = currentTime

            ProjectileAction.DESTROY
        }

        return this
    }
}

