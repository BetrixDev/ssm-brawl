package dev.betrix.superSmashMobsBrawl.abilities

import dev.betrix.superSmashMobsBrawl.events.Damager
import dev.betrix.superSmashMobsBrawl.events.SmashDamageEvent
import dev.betrix.superSmashMobsBrawl.events.SmashDamageType
import dev.betrix.superSmashMobsBrawl.extensions.setVelocity
import dev.betrix.superSmashMobsBrawl.projectiles.BrawlProjectile
import dev.betrix.superSmashMobsBrawl.projectiles.IronHookProjectile
import dev.betrix.superSmashMobsBrawl.projectiles.ProjectileAction
import org.bukkit.Particle
import org.bukkit.Sound
import org.bukkit.entity.LivingEntity
import org.bukkit.entity.Player
import org.bukkit.util.Vector

class IronHookAbility(player: Player) : BrawlAbility("iron_hook", player) {

    private val projectileDamage = 4.0

    private val activeProjectiles = mutableListOf<BrawlProjectile>()

    override fun activate() {
        throwProjectile()
        super.activate()
    }

    override fun teardown() {
        activeProjectiles.forEach { it.teardown() }
        activeProjectiles.clear()
        super.teardown()
    }

    private fun throwProjectile() {
        val hookProjectile =
            IronHookProjectile(player, "abilities.iron_hook.name")
                .onTick { projectile ->
                    val entity = projectile.projectileEntity ?: return@onTick
                    // Ignite sound every tick
                    entity.world.playSound(entity.location, Sound.ITEM_FLINTANDSTEEL_USE, 1f, 1f)
                    // Crit particle every tick
                    Particle.CRIT.builder()
                        .location(entity.location)
                        .count(1)
                        .offset(0.0, 0.0, 0.0)
                        .extra(0.0)
                        .receivers(96, true)
                        .spawn()
                }
                .onHitEntity { entity, projectile ->
                    handleHookHit(entity, projectile)
                    ProjectileAction.DESTROY
                }
                .onHitBlock { _, _ -> ProjectileAction.DESTROY }
                .onTeardown { activeProjectiles.remove(it) }
                .launch()

        activeProjectiles.add(hookProjectile)
    }

    private fun handleHookHit(entity: LivingEntity, projectile: BrawlProjectile) {
        SmashDamageEvent(
                entity,
                Damager.DamagerLivingEntity(player),
                projectileDamage,
                damageType = SmashDamageType.Projectile,
            )
            .callEvent()

        pullEntityTowardsPlayer(entity, projectile.velocityBeforeImpact)
    }

    private fun pullEntityTowardsPlayer(target: LivingEntity, velocityBeforeImpact: Vector) {
        val targetVector = target.location.toVector()
        val playerVector = player.location.toVector()

        val delta = playerVector.subtract(targetVector)
        if (delta.lengthSquared() == 0.0) return

        val trajectory = delta.normalize()
        val mult = (velocityBeforeImpact.length() / 3.0).coerceAtLeast(0.2)

        // Use the shared velocity helper for smoother motion towards the player
        target.setVelocity(trajectory, 0.4 + mult, false, 0.0, 0.2 * mult, 1.2 * mult, true)
    }
}
