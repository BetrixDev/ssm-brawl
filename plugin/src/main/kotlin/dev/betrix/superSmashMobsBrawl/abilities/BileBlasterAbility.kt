package dev.betrix.superSmashMobsBrawl.abilities

import dev.betrix.superSmashMobsBrawl.events.BrawlDamageEvent
import dev.betrix.superSmashMobsBrawl.events.BrawlDamageType
import dev.betrix.superSmashMobsBrawl.events.Damager
import dev.betrix.superSmashMobsBrawl.extensions.doKnockback
import dev.betrix.superSmashMobsBrawl.extensions.playSound
import dev.betrix.superSmashMobsBrawl.extensions.sendDebugMessage
import dev.betrix.superSmashMobsBrawl.projectiles.BrawlProjectile
import dev.betrix.superSmashMobsBrawl.projectiles.ProjectileAction
import gg.flyte.twilight.extension.round
import gg.flyte.twilight.scheduler.TwilightRunnable
import gg.flyte.twilight.scheduler.repeatingTask
import org.bukkit.Material
import org.bukkit.Sound
import org.bukkit.entity.Entity
import org.bukkit.entity.LivingEntity
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.bukkit.util.Vector

class BileBlasterAbility(player: Player) : BrawlAbility("bile_blaster", player) {

    private val projectilesPerTick = metadata.int("projectilesPerTick") ?: 1
    private val spewDurationMs = metadata.long("spewDurationMs") ?: 2000L
    private val projectileKnockbackModifier = metadata.double("projectileKnockbackModifier") ?: 1.0
    private val projectileDamage = metadata.double("projectileDamage") ?: 1.0
    private val projectileSize = (metadata.double("projectileSize") ?: 0.25).coerceAtLeast(0.1)
    private val projectileSpread = metadata.double("projectileSpread") ?: 0.525
    private val projectileBaseVelocity = metadata.double("projectileBaseVelocity") ?: 0.8
    private val projectileVelocityRandomness = metadata.double("projectileVelocityRandomness") ?: 0.2
    private val burpSoundChance = metadata.double("burpSoundChance") ?: 0.15

    private val activeProjectiles = mutableListOf<BrawlProjectile>()
    private var spewTask: TwilightRunnable? = null
    private var spewStartTimeMs: Long = 0

    override fun activate() {
        startSpewing()
        super.activate()
    }

    override fun teardown() {
        stopSpewing()
        activeProjectiles.forEach { it.teardown() }
        activeProjectiles.clear()
        super.teardown()
    }

    private fun startSpewing() {
        stopSpewing()

        spewStartTimeMs = System.currentTimeMillis()

        spewTask = repeatingTask(0) {
            val elapsedTime = System.currentTimeMillis() - spewStartTimeMs

            if (elapsedTime >= spewDurationMs) {
                stopSpewing()
                cancel()
                return@repeatingTask
            }

            // Random burp sound
            if (Math.random() < burpSoundChance) {
                player.playSound(Sound.ENTITY_PLAYER_BURP, volume = 1.0f, pitch = (Math.random() + 0.5).toFloat())
            }

            // Shoot projectiles
            for (i in 0 until projectilesPerTick) {
                throwBileProjectile()
            }
        }

        runnables.add(spewTask!!)
    }

    private fun stopSpewing() {
        spewTask?.cancel()
        spewTask = null
    }

    private fun throwBileProjectile() {
        val bileProjectile = BrawlProjectile.potion(
            player,
            ItemStack.of(Material.ROTTEN_FLESH),
            "abilities.bile_blaster.name"
        )
            .setInitialVelocity { projectile ->
                val random = Vector(
                    (Math.random() - 0.5) * projectileSpread,
                    (Math.random() - 0.5) * projectileSpread,
                    (Math.random() - 0.5) * projectileSpread
                )

                val baseDirection = player.location.direction.add(random)
                val velocity = projectileBaseVelocity + (projectileVelocityRandomness * Math.random())

                projectile.velocity = baseDirection.normalize().multiply(velocity)
            }
            .projectileSize(projectileSize)
            .onHitEntity { entity, projectile ->
                handleBileHit(entity, projectile)
                ProjectileAction.DESTROY
            }
            .onHitBlock { _, projectile ->
                handleBileHit(null, projectile)
                ProjectileAction.DESTROY
            }
            .onExpire { projectile ->
                handleBileHit(null, projectile)
                ProjectileAction.DESTROY
            }
            .onTeardown { activeProjectiles.remove(it) }
            .launch()

        activeProjectiles.add(bileProjectile)
    }

    private fun handleBileHit(entity: Entity?, projectile: BrawlProjectile) {
        player.sendDebugMessage(
            "[BB] Bile projectile hit at ${projectile.projectileEntity?.x?.round(1)}, ${
                projectile.projectileEntity?.y?.round(1)
            }, ${projectile.projectileEntity?.z?.round(1)} after ${projectile.projectileEntity?.ticksLived} ticks"
        )

        if (entity != null && entity is LivingEntity) {
            player.sendDebugMessage("[BB] ${entity.name} was hit")

            val damageEvent = BrawlDamageEvent(
                entity,
                Damager.DamagerLivingEntity(player),
                projectileDamage,
                0.0,
                BrawlDamageType.Projectile
            )

            damageEvent.callEvent()

            entity.doKnockback(
                projectileKnockbackModifier,
                projectileDamage,
                entity.health,
                projectile.projectileEntity?.location?.toVector(),
                null
            )
        } else {
            player.sendDebugMessage("[BB] No entity was hit")
        }
    }
}
