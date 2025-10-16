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
import org.bukkit.*
import org.bukkit.entity.LivingEntity
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.PotionMeta
import org.bukkit.potion.PotionEffect
import org.bukkit.potion.PotionEffectType

class DazePotionAbility(player: Player) : BrawlAbility("daze_potion", player) {

    private val projectileDamage = metadata.double("projectileDamage") ?: 7.0
    private val projectileKnockbackModifier = metadata.double("projectileKnockbackModifier") ?: 2.0
    private val projectileVelocityMultiplier =
        metadata.double("projectileVelocityMultiplier") ?: 1.0
    private val expirationTicks = metadata.long("expirationTicks") ?: 200L
    private val maxBounces = metadata.int("maxBounces") ?: 3
    private val effectRadius = metadata.double("effectRadius") ?: 3.0
    private val potionDurationTicks = metadata.int("potionDurationTicks") ?: 40
    private val potionAmplifier = metadata.int("potionAmplifier") ?: 1

    private val activeProjectiles = mutableListOf<BrawlProjectile>()

    override fun activate() {
        throwDazePotion()
        super.activate()
    }

    override fun teardown() {
        activeProjectiles.forEach { it.teardown() }
        activeProjectiles.clear()
        super.teardown()
    }

    private fun throwDazePotion() {
        // Create potion item with visual effect
        val potionItem =
            ItemStack(Material.SPLASH_POTION).apply {
                itemMeta = (itemMeta as? PotionMeta)?.apply { color = Color.GRAY }
            }

        var bounceCount = 0

        val dazeProjectile =
            BrawlProjectile.potion(player, potionItem, "abilities.daze_potion.name")
                .velocityMultiplier(projectileVelocityMultiplier)
                .maxLifetime(expirationTicks)
                .enableEntityDetection(false) // Legacy behavior: no direct entity hits
                .enableBlockDetection(true) // Need this for bouncing
                .enableIdleDetection(false) // Legacy behavior: no idle detection
                .trailEffect(Particle.SMOKE, count = 2)
                .onHitBlock { block, projectile ->
                    player.sendDebugMessage(
                        "[DP] Potion bounced off block at ${block.x}, ${block.y}, ${block.z} (bounce $bounceCount/$maxBounces)"
                    )

                    bounceCount++

                    if (bounceCount >= maxBounces) {
                        player.sendDebugMessage("[DP] Max bounces reached, exploding")
                        handlePotionSplash(projectile.projectileEntity?.location)
                        ProjectileAction.DESTROY
                    } else {
                        player.playSound(Sound.BLOCK_GLASS_HIT, volume = 0.5f, pitch = 1.2f)
                        ProjectileAction.BOUNCE
                    }
                }
                .onExpire { projectile ->
                    player.sendDebugMessage("[DP] Potion expired, exploding")
                    handlePotionSplash(projectile.projectileEntity?.location)
                    ProjectileAction.DESTROY
                }
                .onTeardown { activeProjectiles.remove(it) }
                .launch()

        activeProjectiles.add(dazeProjectile)
    }

    private fun handlePotionSplash(location: Location?) {
        if (location == null) {
            player.sendDebugMessage("[DP] No location for splash")
            return
        }

        // Play splash effects
        location.world.playSound(location, Sound.BLOCK_GLASS_BREAK, 2f, 1.0f)
        location.world.playSound(location, Sound.ENTITY_SPLASH_POTION_BREAK, 2f, 1.0f)

        location.world.spawnParticle(
            Particle.ITEM,
            location,
            30,
            0.5,
            0.5,
            0.5,
            0.1,
            ItemStack(Material.GRAY_DYE),
        )

        location.world.spawnParticle(
            Particle.CLOUD,
            location,
            20,
            effectRadius / 2,
            effectRadius / 2,
            effectRadius / 2,
            0.05,
        )

        // Find and affect nearby entities
        val affectedCount =
            location
                .getNearbyEntities(effectRadius, effectRadius, effectRadius)
                .filterIsInstance<LivingEntity>()
                .filter { it != player }
                .onEach { entity ->
                    // Apply damage
                    val damageEvent =
                        BrawlDamageEvent(
                            entity,
                            Damager.DamagerLivingEntity(player),
                            projectileDamage,
                            projectileKnockbackModifier,
                            BrawlDamageType.Explosion,
                        )
                    damageEvent.callEvent()

                    // Apply knockback
                    entity.doKnockback(
                        projectileKnockbackModifier,
                        projectileDamage,
                        entity.health,
                        location.toVector(),
                        null,
                    )

                    // Apply slowness effect
                    entity.removePotionEffect(PotionEffectType.SLOWNESS)
                    entity.addPotionEffect(
                        PotionEffect(
                            PotionEffectType.SLOWNESS,
                            potionDurationTicks,
                            potionAmplifier,
                        )
                    )

                    player.sendDebugMessage(
                        "[DP] ${entity.name} hit at distance ${entity.location.distance(location).round(1)}"
                    )
                }
                .size

        player.sendDebugMessage("[DP] Splash affected $affectedCount entities")
    }
}
