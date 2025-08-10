package dev.betrix.superSmashMobsBrawl.abilities

import dev.betrix.superSmashMobsBrawl.events.Damager
import dev.betrix.superSmashMobsBrawl.events.SmashDamageEvent
import dev.betrix.superSmashMobsBrawl.events.SmashDamageType
import dev.betrix.superSmashMobsBrawl.extensions.doKnockback
import dev.betrix.superSmashMobsBrawl.extensions.sendDebugMessage
import dev.betrix.superSmashMobsBrawl.projectiles.SulphurBombProjectile
import gg.flyte.twilight.extension.round
import org.bukkit.entity.Player

class SulphurBombAbility(player: Player) : BrawlAbility("sulphur_bomb", player) {

    private val projectileKnockbackModifier = metadata.double("projectileKnockbackModifier") ?: 2.5
    private val projectileDamage = metadata.double("projectileDamage") ?: 6.5

    override fun activate() {
        throwProjectile()
        super.activate()
    }

    private fun throwProjectile() {
        val projectile =
            SulphurBombProjectile(player) { hitEntity, projectile ->
                player.sendDebugMessage(
                    "[SB] Sulphur bomb exploded at ${projectile?.x?.round(1)}, ${
                        projectile?.y?.round(
                            1
                        )
                    }, ${projectile?.z?.round(1)} after ${projectile?.ticksLived} ticks"
                )

                if (hitEntity != null) {
                    player.sendDebugMessage("[SB] ${hitEntity.name} was hit")

                    val damageEvent =
                        SmashDamageEvent(
                            hitEntity,
                            Damager.DamagerLivingEntity(player),
                            projectileDamage,
                            0.0,
                            SmashDamageType.Projectile,
                        )

                    damageEvent.callEvent()

                    hitEntity.doKnockback(
                        projectileKnockbackModifier,
                        projectileDamage,
                        hitEntity.health,
                        projectile?.location?.toVector(),
                        null,
                    )
                } else {
                    player.sendDebugMessage("[SB] No entity was hit")
                }

                true
            }

        projectile.launch()
    }
}
