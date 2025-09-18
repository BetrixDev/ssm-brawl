package dev.betrix.superSmashMobsBrawl.abilities

import dev.betrix.superSmashMobsBrawl.events.Damager
import dev.betrix.superSmashMobsBrawl.events.BrawlDamageEvent
import dev.betrix.superSmashMobsBrawl.events.BrawlDamageType
import dev.betrix.superSmashMobsBrawl.extensions.setVelocity
import dev.betrix.superSmashMobsBrawl.projectiles.BrawlProjectile
import dev.betrix.superSmashMobsBrawl.projectiles.ProjectileAction
import org.bukkit.Location
import org.bukkit.Sound
import org.bukkit.entity.Player
import org.bukkit.util.Vector

class RopedArrowAbility(player: Player) : BrawlAbility("roped_arrow", player) {

    private val arrowVelocityModifier = metadata.double("arrowVelocityModifier") ?: 2.4
    private val arrowDamage = metadata.double("arrowDamage") ?: 6.0

    private val activeProjectiles = mutableListOf<BrawlProjectile>()

    override fun activate() {
        super.activate()

        val ropedArrow =
            BrawlProjectile.arrow(player, arrowVelocityModifier, "abilities.roped_arrow.name")
                .onHitBlock { block, projectile ->
                    pullPlayerToLocation(block.location, projectile.velocityBeforeImpact)
                    ProjectileAction.DESTROY
                }
                .onHitEntity { entity, projectile ->
                    pullPlayerToLocation(entity.location, projectile.velocityBeforeImpact)

                    BrawlDamageEvent(
                            entity,
                            Damager.DamagerLivingEntity(player),
                            arrowDamage,
                            damageType = BrawlDamageType.Projectile,
                        )
                        .callEvent()

                    ProjectileAction.DESTROY
                }
                .onTeardown { activeProjectiles.remove(it) }
                .launch()

        activeProjectiles.add(ropedArrow)
    }

    override fun teardown() {
        activeProjectiles.forEach { it.teardown() }
        activeProjectiles.clear()
        super.teardown()
    }

    private fun pullPlayerToLocation(location: Location, velocity: Vector) {
        val playerLocationVector = player.location.toVector()
        val arrowLocationVector = location.toVector()

        val delta = arrowLocationVector.subtract(playerLocationVector)

        if (delta.lengthSquared() == 0.0) {
            return
        }

        val trajectory = delta.normalize()
        val mult = velocity.length() / 3.0

        player.setVelocity(trajectory, 0.4 + mult, false, 0.0, 0.2 * mult, 1.2 * mult, true)

        location.world.playSound(location, Sound.ENTITY_ARROW_HIT, 2.5f, 0.6f)
    }
}
