package dev.betrix.superSmashMobsBrawl.abilities

import dev.betrix.superSmashMobsBrawl.events.BrawlDamageEvent
import dev.betrix.superSmashMobsBrawl.events.BrawlDamageType
import dev.betrix.superSmashMobsBrawl.events.Damager
import dev.betrix.superSmashMobsBrawl.extensions.setVelocity
import dev.betrix.superSmashMobsBrawl.projectiles.BrawlProjectile
import dev.betrix.superSmashMobsBrawl.projectiles.ProjectileAction
import org.bukkit.Material
import org.bukkit.Particle
import org.bukkit.Sound
import org.bukkit.entity.Item
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack

class WhirlpoolAxeAbility(player: Player) : BrawlAbility("whirlpool_axe", player) {

    private val damage = metadata.double("damage") ?: 4.0
    private val hitboxSize = metadata.double("hitboxSize") ?: 0.5
    private val knockbackMultiplier = metadata.double("knockbackMultiplier") ?: 0.0
    private val expirationTicks = metadata.long("expirationTicks") ?: 60L
    private val velocityMultiplier = metadata.double("velocityMultiplier") ?: 1.6
    private val pullYStrength = metadata.double("pullYStrength") ?: 0.5

    override fun activate() {
        super.activate()

        player.world.playSound(player.location, Sound.BLOCK_SNOW_BREAK, 1f, 1f)

        val projectile =
            object : BrawlProjectile(player, "whirlpool_axe") {
                override fun createProjectileEntity() =
                    player.world
                        .dropItem(player.eyeLocation, ItemStack(Material.PRISMARINE_SHARD))
                        .apply {
                            pickupDelay = Int.MAX_VALUE
                            setCanPlayerPickup(false)
                            setCanMobPickup(false)
                        }
            }

        projectile
            .velocityMultiplier(velocityMultiplier)
            .projectileSize(hitboxSize)
            .maxLifetime(expirationTicks)
            .onTick {
                val entity = it.projectileEntity
                if (entity != null && entity.isValid) {
                    Particle.DRIPPING_WATER
                        .builder()
                        .location(entity.location)
                        .offset(0.0, 0.0, 0.0)
                        .count(1)
                        .extra(0.01)
                        .receivers(96, true)
                        .spawn()
                }
            }
            .onHitEntity { entity, _ ->
                val damageEvent =
                    BrawlDamageEvent(
                        entity,
                        Damager.DamagerLivingEntity(player),
                        damage,
                        knockbackMultiplier = knockbackMultiplier,
                        damageType = BrawlDamageType.Projectile,
                    )
                damageEvent.callEvent()

                // Pull the entity towards the player
                val trajectory =
                    player.location.toVector().subtract(entity.location.toVector()).normalize()
                trajectory.y = pullYStrength

                entity.setVelocity(
                    trajectory,
                    strength = 1.0,
                    ySet = false,
                    yBase = 0.0,
                    yAdd = 0.0,
                    yMax = 10.0,
                    groundBoost = false,
                )

                ProjectileAction.DESTROY
            }
            .onHitBlock { _, _ -> ProjectileAction.DESTROY }
            .onExpire { ProjectileAction.DESTROY }
            .onIdle { ProjectileAction.DESTROY }
            .launch()
    }
}
