package dev.betrix.superSmashMobsBrawl.events

import dev.betrix.superSmashMobsBrawl.minigames.instances.MinigameInstance
import gg.flyte.twilight.event.TwilightEvent
import org.bukkit.entity.LivingEntity
import org.bukkit.entity.Player

sealed class SmashDamageType {
    object Projectile : SmashDamageType()

    object Explosion : SmashDamageType()
}

class SmashDamageEvent(
    val victim: LivingEntity,
    val damager: LivingEntity?,
    val damage: Double,
    val knockbackMultiplier: Double = 1.0,
    val damageType: SmashDamageType? = null,
) : TwilightEvent() {

    fun isValid(minigame: MinigameInstance): Boolean {
        // TODO: Once we figure out how we want to handle all living entities in minigames, this
        // check will be removed
        if (victim !is Player || damager !is Player) {
            return false
        }

        return minigame.isPlayerInMinigame(damager) && minigame.isPlayerInMinigame(victim)
    }
}
