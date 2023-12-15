package dev.betrix.supersmashmobsbrawl.listeners

import dev.betrix.supersmashmobsbrawl.SSMBPlayer
import dev.betrix.supersmashmobsbrawl.enums.TaggedKeyNum
import dev.betrix.supersmashmobsbrawl.enums.TaggedKeyStr
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

        val projectileDamage = splashedItem.getMetadata(TaggedKeyNum.PROJECTILE_DAMAGE)!!
        val projectileDamageRange = splashedItem.getMetadata(TaggedKeyNum.PROJECTILE_DAMAGE_RANGE) ?: 1.0
        val projectileExplosionParticle = splashedItem.getMetadata(TaggedKeyStr.PROJECTILE_EXPLOSION_PARTICLE)!!
        val projectileExplosionSound = splashedItem.getMetadata(TaggedKeyStr.PROJECTILE_EXPLOSION_SOUND)!!

        if (event.hitEntity != null && event.hitEntity is Player && event.hitEntity != thrower.bukkitPlayer) {
            val target = event.hitEntity as Player
            target.damage(projectileDamage)
        } else if (event.hitBlock != null) {
            thrower.bukkitPlayer.world.players.forEach {
                if (it.location.distance(splashedLocation) <= projectileDamageRange && it.player != thrower.bukkitPlayer) {
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