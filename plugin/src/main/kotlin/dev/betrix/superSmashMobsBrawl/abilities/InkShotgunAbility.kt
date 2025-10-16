package dev.betrix.superSmashMobsBrawl.abilities

import dev.betrix.superSmashMobsBrawl.events.BrawlDamageEvent
import dev.betrix.superSmashMobsBrawl.events.BrawlDamageType
import dev.betrix.superSmashMobsBrawl.events.Damager
import dev.betrix.superSmashMobsBrawl.extensions.doKnockback
import dev.betrix.superSmashMobsBrawl.extensions.playSound
import dev.betrix.superSmashMobsBrawl.extensions.sendDebugMessage
import dev.betrix.superSmashMobsBrawl.projectiles.BrawlProjectile
import dev.betrix.superSmashMobsBrawl.projectiles.HitType
import dev.betrix.superSmashMobsBrawl.projectiles.ProjectileAction
import gg.flyte.twilight.extension.round
import org.bukkit.Color
import org.bukkit.Material
import org.bukkit.Particle
import org.bukkit.Sound
import org.bukkit.entity.Entity
import org.bukkit.entity.LivingEntity
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.bukkit.util.Vector

class InkShotgunAbility(player: Player) : BrawlAbility("ink_shotgun", player) {

    private val inkAmount = metadata.int("inkAmount") ?: 7
    private val projectileKnockbackModifier = metadata.double("projectileKnockbackModifier") ?: 3.0
    private val projectileDamage = metadata.double("projectileDamage") ?: 1.725
    private val projectileSize = (metadata.double("projectileSize") ?: 0.5).coerceAtLeast(0.1)
    private val projectileSpread = metadata.double("projectileSpread") ?: 1.0
    private val projectileBaseVelocity = metadata.double("projectileBaseVelocity") ?: 1.0
    private val projectileVelocityRandomness =
        metadata.double("projectileVelocityRandomness") ?: 0.4

    private val activeProjectiles = mutableListOf<BrawlProjectile>()

    override fun activate() {
        shootInkPellets()
        super.activate()
    }

    override fun teardown() {
        activeProjectiles.forEach { it.teardown() }
        activeProjectiles.clear()
        super.teardown()
    }

    private fun shootInkPellets() {
        player.playSound(Sound.ENTITY_GENERIC_EXPLODE, volume = 1.5f, pitch = 0.75f)
        player.playSound(Sound.ENTITY_GENERIC_SPLASH, volume = 0.75f, pitch = 1.0f)

        for (i in 0 until inkAmount) {
            val spread = if (i == 0) 0.0 else projectileSpread
            throwInkProjectile(spread)
        }
    }

    private fun throwInkProjectile(spread: Double) {
        val inkProjectile =
            BrawlProjectile.potion(
                    player,
                    ItemStack.of(Material.INK_SAC),
                    "abilities.ink_shotgun.name",
                )
                .setInitialVelocity { projectile ->
                    val random =
                        if (spread > 0) {
                            Vector(
                                    (Math.random() - 0.5),
                                    (Math.random() - 0.5),
                                    (Math.random() - 0.5),
                                )
                                .multiply(spread)
                                .normalize()
                                .multiply(0.15)
                        } else {
                            Vector(0.0, 0.0, 0.0)
                        }

                    val baseDirection = player.location.direction.add(random)
                    val velocity =
                        projectileBaseVelocity + (projectileVelocityRandomness * Math.random())

                    projectile.velocity = baseDirection.normalize().multiply(velocity)
                }
                .projectileSize(projectileSize)
                .impactEffect(Particle.BUBBLE_POP)
                .addEffect(
                    object : dev.betrix.superSmashMobsBrawl.projectiles.ProjectileEffect {
                        override fun onTick(projectile: BrawlProjectile) {
                            if ((projectile.projectileEntity?.ticksLived ?: 0) < 2) return

                            Particle.DUST.builder()
                                .count(1)
                                .color(Color.fromARGB(128, 128, 0, 128))
                                .location(projectile.projectileEntity?.location ?: return)
                                .receivers(96, true)
                                .spawn()
                        }

                        override fun onHit(projectile: BrawlProjectile, hitType: HitType) {}
                    }
                )
                .onHitEntity { entity, projectile ->
                    handleInkHit(entity, projectile)
                    ProjectileAction.DESTROY
                }
                .onHitBlock { _, projectile ->
                    handleInkHit(null, projectile)
                    ProjectileAction.DESTROY
                }
                .onExpire { projectile ->
                    handleInkHit(null, projectile)
                    ProjectileAction.DESTROY
                }
                .onIdle { projectile ->
                    handleInkHit(null, projectile)
                    ProjectileAction.DESTROY
                }
                .onTeardown { activeProjectiles.remove(it) }
                .launch()

        activeProjectiles.add(inkProjectile)
    }

    private fun handleInkHit(entity: Entity?, projectile: BrawlProjectile) {
        player.sendDebugMessage(
            "[IS] Ink pellet hit at ${projectile.projectileEntity?.x?.round(1)}, ${
                projectile.projectileEntity?.y?.round(1)
            }, ${projectile.projectileEntity?.z?.round(1)} after ${projectile.projectileEntity?.ticksLived} ticks"
        )

        if (entity != null && entity is LivingEntity) {
            player.sendDebugMessage("[IS] ${entity.name} was hit")

            val damageEvent =
                BrawlDamageEvent(
                    entity,
                    Damager.DamagerLivingEntity(player),
                    projectileDamage,
                    0.0,
                    BrawlDamageType.Projectile,
                )

            //            damageEvent.ignoreDamageDelay = true
            damageEvent.callEvent()

            entity.doKnockback(
                projectileKnockbackModifier,
                projectileDamage,
                entity.health,
                projectile.projectileEntity?.location?.toVector(),
                null,
            )
        } else {
            player.sendDebugMessage("[IS] No entity was hit")
        }
    }

    private fun explodeEffect(projectile: BrawlProjectile) {
        val entity = projectile.projectileEntity ?: return
        entity.world.playSound(entity.location, Sound.ENTITY_GENERIC_EXPLODE, 0.75f, 1.25f)
    }
}
