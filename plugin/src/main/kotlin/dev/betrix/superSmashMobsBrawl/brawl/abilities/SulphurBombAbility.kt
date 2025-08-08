package dev.betrix.superSmashMobsBrawl.brawl.abilities

import dev.betrix.superSmashMobsBrawl.abilities.AbilityMetadata
import dev.betrix.superSmashMobsBrawl.brawl.BrawlAbility
import dev.betrix.superSmashMobsBrawl.events.Damager
import dev.betrix.superSmashMobsBrawl.events.SmashDamageEvent
import dev.betrix.superSmashMobsBrawl.events.SmashDamageType
import dev.betrix.superSmashMobsBrawl.extensions.doKnockback
import dev.betrix.superSmashMobsBrawl.projectiles.SulphurBombProjectile
import org.bukkit.entity.Player

class SulphurBombAbility(id: String, player: Player, metadata: AbilityMetadata) :
    BrawlAbility(id, player, metadata) {

    private val projectileKnockbackModifier = 2.5
    private val projectileDamage = 6.5

    override fun activate() {
        setCooldown()
        throwProjectile()

        player.sendMessage("§7Sulphur Bomb thrown!")
    }

    private fun throwProjectile() {
        val projectile =
            SulphurBombProjectile(player) { hitEntity, projectile ->
                if (hitEntity != null) {
                    val damageEvent =
                        SmashDamageEvent(
                            hitEntity,
                            Damager.LivingEntity(player),
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
                }

                true
            }

        projectile.launch()
    }
}
