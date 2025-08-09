package dev.betrix.superSmashMobsBrawl.abilities

import dev.betrix.superSmashMobsBrawl.events.Damager
import dev.betrix.superSmashMobsBrawl.events.SmashDamageEvent
import dev.betrix.superSmashMobsBrawl.events.SmashDamageType
import dev.betrix.superSmashMobsBrawl.extensions.setVelocity
import dev.betrix.superSmashMobsBrawl.projectiles.ArrowProjectile
import org.bukkit.Location
import org.bukkit.Sound
import org.bukkit.entity.Player
import org.bukkit.util.Vector

class RopedArrowAbility(player: Player) : BrawlAbility("roped_arrow", player) {

    override fun activate() {
        super.activate()

        val projectile =
            ArrowProjectile(player, "Roped Arrow", 2.4)
                .onHitBlock { block, projectile ->
                    pullPlayerToLocation(block.location, projectile.velocityBeforeImpact)
                    true
                }
                .onHitLivingEntity { entity, projectile ->
                    pullPlayerToLocation(entity.location, projectile.velocityBeforeImpact)

                    val damageEvent =
                        SmashDamageEvent(
                            entity,
                            Damager.LivingEntity(player),
                            6.0,
                            damageType = SmashDamageType.Projectile,
                        )

                    damageEvent.callEvent()

                    true
                }

        projectile.launch()
    }

    private fun pullPlayerToLocation(location: Location, velocity: Vector) {
        val playerLocationVector = player.location.toVector()
        val arrowLocationVector = location.toVector()

        val pre = arrowLocationVector.subtract(playerLocationVector)
        val trajectory = pre.normalize()
        val mult = velocity.length() / 3.0

        player.setVelocity(trajectory, 0.4 + mult, false, 0.0, 0.2 * mult, 1.2 * mult, true)

        location.world.playSound(location, Sound.ENTITY_ARROW_HIT, 2.5f, 0.6f)
    }
}
