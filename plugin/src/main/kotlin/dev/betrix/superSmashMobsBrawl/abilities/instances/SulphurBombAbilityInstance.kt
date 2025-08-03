package dev.betrix.superSmashMobsBrawl.abilities.instances

import dev.betrix.superSmashMobsBrawl.abilities.definitions.AbilityDefinition
import dev.betrix.superSmashMobsBrawl.events.Damager
import dev.betrix.superSmashMobsBrawl.events.SmashDamageEvent
import dev.betrix.superSmashMobsBrawl.events.SmashDamageType
import dev.betrix.superSmashMobsBrawl.extensions.doKnockback
import dev.betrix.superSmashMobsBrawl.projectiles.SulphurBombProjectile
import org.bukkit.entity.Player

class SulphurBombAbilityInstance(definition: AbilityDefinition, player: Player) :
    AbilityInstance(definition, player) {

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
                        player.location.toVector(),
                        null,
                    )
                }

                true
            }

        projectile.launch()
    }
}
