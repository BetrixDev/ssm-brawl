package dev.betrix.supersmashmobsbrawl.listeners

import dev.betrix.supersmashmobsbrawl.SSMBPlayer
import dev.betrix.supersmashmobsbrawl.enums.TaggedKeyNum
import dev.betrix.supersmashmobsbrawl.enums.TaggedKeyStr
import dev.betrix.supersmashmobsbrawl.extensions.doKnockback
import dev.betrix.supersmashmobsbrawl.extensions.getMetadata
import org.bukkit.Particle
import org.bukkit.Sound
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.entity.PotionSplashEvent

class PotionSplashListener : Listener {

    @EventHandler
    fun onPotionSplash(event: PotionSplashEvent) {
        val splashedItem = event.entity
        val splashedLocation = splashedItem.location

        val thrower =
            splashedItem.ownerUniqueId?.let {
                SSMBPlayer.fromUuid(
                    it
                )
            } ?: return

        event.isCancelled = true

        val projectKnockbackMultiplier = splashedItem.getMetadata(TaggedKeyNum.PROJECTILE_KNOCKBACK_MULTIPLIER)!!
        val projectileDamage = splashedItem.getMetadata(TaggedKeyNum.PROJECTILE_DAMAGE)!!
        val projectileDamageAoe = splashedItem.getMetadata(TaggedKeyNum.PROJECTILE_DAMAGE_AOE)
        val projectileExplosionParticle = splashedItem.getMetadata(TaggedKeyStr.PROJECTILE_EXPLOSION_PARTICLE)!!
        val projectileExplosionSound = splashedItem.getMetadata(TaggedKeyStr.PROJECTILE_EXPLOSION_SOUND)!!

        if (event.hitEntity != null && event.hitEntity is Player && event.hitEntity != thrower.bukkitPlayer) {
            val target = event.hitEntity as Player
            target.doKnockback(
                projectKnockbackMultiplier,
                projectileDamage,
                target.health,
                thrower.bukkitPlayer.location.toVector(),
                null
            )
            target.damage(projectileDamage, splashedItem)
        } else if (event.hitBlock != null && projectileDamageAoe != null) {
            thrower.bukkitPlayer.world.players.forEach {
                if (it.location.distance(splashedLocation) <= projectileDamageAoe && it.player != thrower.bukkitPlayer) {
                    it.doKnockback(
                        projectKnockbackMultiplier,
                        projectileDamage,
                        it.health,
                        thrower.bukkitPlayer.location.toVector(),
                        null
                    )
                    it.damage(projectileDamage)
                }
            }
        }

        thrower.bukkitPlayer.world.spawnParticle(
            Particle.valueOf(projectileExplosionParticle.uppercase()),
            splashedLocation,
            1
        )
        thrower.bukkitPlayer.world.playSound(
            splashedLocation,
            Sound.valueOf(projectileExplosionSound.uppercase()),
            1F,
            1.5F
        )
    }
}