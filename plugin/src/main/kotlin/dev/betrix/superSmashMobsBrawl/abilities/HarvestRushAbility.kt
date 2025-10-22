package dev.betrix.superSmashMobsBrawl.abilities

import dev.betrix.superSmashMobsBrawl.events.BrawlDamageEvent
import dev.betrix.superSmashMobsBrawl.events.BrawlDamageType
import dev.betrix.superSmashMobsBrawl.events.Damager
import dev.betrix.superSmashMobsBrawl.extensions.setVelocity
import dev.betrix.superSmashMobsBrawl.projectiles.BrawlProjectile
import dev.betrix.superSmashMobsBrawl.projectiles.ProjectileAction
import gg.flyte.twilight.scheduler.TwilightRunnable
import gg.flyte.twilight.scheduler.repeatingTask
import org.bukkit.Material
import org.bukkit.Sound
import org.bukkit.entity.LivingEntity
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.bukkit.util.Vector

class HarvestRushAbility(player: Player) : BrawlAbility("harvest_rush", player) {

    private val durationMs: Long = metadata.long("durationMs") ?: 1500L
    private val projectileDamage: Double = metadata.double("projectileDamage") ?: 0.2
    private val projectilesPerTick: Int = metadata.int("projectilesPerTick") ?: 6
    private val dashSpeed: Double = metadata.double("dashSpeed") ?: 0.6
    private val yLimit: Double = metadata.double("yLimit") ?: 0.0
    private val slamDamage: Double = metadata.double("slamDamage") ?: 4.0
    private val slamRadius: Double = metadata.double("slamRadius") ?: 3.5
    private val slamKnockbackMultiplier: Double =
        metadata.double("slamKnockbackMultiplier") ?: 2.5
    private val cancelTrigger: String = metadata.string("cancelTrigger") ?: "SNEAK"

    private var rushTask: TwilightRunnable? = null
    private val activeProjectiles = mutableListOf<dev.betrix.superSmashMobsBrawl.projectiles.BrawlProjectile>()
    private var rushStartTime: Long = 0

    override fun activate() {
        super.activate()
        startRush()
    }

    override fun teardown() {
        rushTask?.cancel()
        rushTask = null
        activeProjectiles.forEach { it.teardown() }
        activeProjectiles.clear()
        super.teardown()
    }

    private fun startRush() {
        rushTask?.cancel()
        rushStartTime = System.currentTimeMillis()

        rushTask =
            repeatingTask(0, 0) {
                if (!player.isValid || player.isDead) {
                    cancel()
                    rushTask = null
                    return@repeatingTask
                }

                val elapsed = System.currentTimeMillis() - rushStartTime

                // Check for early cancel
                if (shouldCancel()) {
                    cancel()
                    rushTask = null
                    performSlam()
                    return@repeatingTask
                }

                // Check if duration expired
                if (elapsed >= durationMs) {
                    cancel()
                    rushTask = null
                    return@repeatingTask
                }

                // Play sound
                player.world.playSound(
                    player.location,
                    Sound.ENTITY_SKELETON_HURT,
                    0.4f,
                    (Math.random() + 1).toFloat(),
                )

                // Move player forward with Y-locked velocity
                if (!player.isSneaking) {
                    player.setVelocity(
                        player.location.direction,
                        dashSpeed,
                        false,
                        0.0,
                        0.0,
                        yLimit,
                        false,
                    )
                }

                // Spawn harvest projectiles
                for (i in 0 until projectilesPerTick) {
                    spawnHarvestProjectile()
                }
            }

        rushTask?.let { runnables.add(it) }
    }

    private fun shouldCancel(): Boolean {
        return when (cancelTrigger.uppercase()) {
            "SNEAK" -> player.isSneaking
            "SHIFT" -> player.isSneaking
            else -> false
        }
    }

    private fun spawnHarvestProjectile() {
        // Randomly select between apple, wheat, and sugar
        val harvestItem = when ((Math.random() * 3).toInt()) {
            0 -> Material.APPLE
            1 -> Material.WHEAT
            else -> Material.SUGAR
        }

        val harvestProjectile =
            BrawlProjectile.potion(
                    player,
                    ItemStack.of(harvestItem),
                    "abilities.harvest_rush.name",
                )
                .setInitialVelocity { projectile ->
                    val random =
                        Vector(
                            (Math.random() - 0.5) * 0.5,
                            (Math.random() - 0.5) * 0.5,
                            (Math.random() - 0.5) * 0.5,
                        )

                    val baseDirection = player.location.direction.add(random)
                    val velocity = 0.8 + (Math.random() * 0.3)

                    projectile.velocity = baseDirection.normalize().multiply(velocity)
                }
                .projectileSize(0.4)
                .maxLifetime(40)
                .onHitEntity { entity, _ ->
                    if (canDamageEntity(entity)) {
                        BrawlDamageEvent(
                                entity,
                                Damager.DamagerLivingEntity(player),
                                projectileDamage,
                                0.5,
                                BrawlDamageType.Projectile,
                            )
                            .callEvent()
                    }
                    ProjectileAction.DESTROY
                }
                .onHitBlock { _, _ -> ProjectileAction.DESTROY }
                .onExpire { ProjectileAction.DESTROY }
                .onTeardown { activeProjectiles.remove(it) }
                .launch()

        activeProjectiles.add(harvestProjectile)
    }

    private fun performSlam() {
        val ownerLocation = player.location

        // Deal damage to nearby entities
        ownerLocation
            .getNearbyEntities(slamRadius, slamRadius, slamRadius)
            .mapNotNull { it as? LivingEntity }
            .filter { it != player }
            .filter { canDamageEntity(it) }
            .forEach { target ->
                val distance = target.location.distance(ownerLocation)
                val normalizedDistance =
                    ((slamRadius - distance) / slamRadius).coerceAtLeast(0.0)
                val scaledDamage = (slamDamage * normalizedDistance) + 0.5

                BrawlDamageEvent(
                        target,
                        Damager.DamagerLivingEntity(player),
                        scaledDamage,
                        slamKnockbackMultiplier,
                        BrawlDamageType.Explosion,
                    )
                    .callEvent()
            }

        // Play sound and particles
        ownerLocation.world.playSound(ownerLocation, Sound.ENTITY_ZOMBIE_ATTACK_WOODEN_DOOR, 2f, 0.8f)
        ownerLocation.world.spawnParticle(
            org.bukkit.Particle.EXPLOSION,
            ownerLocation,
            1,
            0.0,
            0.0,
            0.0,
            0.0,
        )
    }

    private fun canDamageEntity(entity: LivingEntity): Boolean {
        if (entity !is Player) {
            return true
        }

        val minigame = minigameService.getMinigameForPlayer(player) ?: return true

        return !minigame.arePlayersOnSameTeam(player, entity)
    }
}

