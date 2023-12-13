package dev.betrix.supersmashmobsbrawl.listeners

import dev.betrix.supersmashmobsbrawl.SSMBPlayer
import dev.betrix.supersmashmobsbrawl.enums.TaggedKey
import dev.betrix.supersmashmobsbrawl.extensions.getDouble
import dev.betrix.supersmashmobsbrawl.extensions.getString
import org.bukkit.Particle
import org.bukkit.Sound
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.entity.PotionSplashEvent
import java.util.*

class PotionSplashListener : Listener {

    @EventHandler
    fun onPotionSplash(event: PotionSplashEvent) {
        val splashedItem = event.entity
        val splashedLocation = splashedItem.location

        if (!splashedItem.persistentDataContainer.has(TaggedKey.PROJECTILE_THROWER_UUID.key)) {
            return
        }

        val thrower =
            SSMBPlayer.fromUuid(
                UUID.fromString(
                    splashedItem.persistentDataContainer.getString(TaggedKey.PROJECTILE_THROWER_UUID)
                )
            ) ?: return

        event.isCancelled = true

        val projectileDamage =
            splashedItem.persistentDataContainer.getDouble(TaggedKey.PROJECTILE_DAMAGE)!!
        val projectileDamageRange =
            splashedItem.persistentDataContainer.getDouble(TaggedKey.PROJECTILE_DAMAGE_RANGE)!!
        val projectileExplosionParticle =
            splashedItem.persistentDataContainer.getString(TaggedKey.PROJECTILE_EXPLOSION_PARTICLE)!!
        val projectileExplosionSound =
            splashedItem.persistentDataContainer.getString(TaggedKey.PROJECT_EXPLOSION_SOUND)!!

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

        thrower.bukkitPlayer.world.spawnParticle(Particle.valueOf(projectileExplosionParticle), splashedLocation, 1)
        thrower.bukkitPlayer.world.playSound(splashedLocation, Sound.valueOf(projectileExplosionSound), 1F, 1.5F)
    }
}