package dev.betrix.superSmashMobsBrawl.events

import dev.betrix.superSmashMobsBrawl.minigames.BrawlMinigame
import dev.betrix.superSmashMobsBrawl.models.brawlData.MinigameDef
import gg.flyte.twilight.event.TwilightEvent
import org.bukkit.entity.LivingEntity
import org.bukkit.entity.Player

sealed class SmashDamageType {
    object Projectile : SmashDamageType()

    object Explosion : SmashDamageType()
}

sealed class Damager {
    object System : Damager()

    data class LivingEntity(val livingEntity: org.bukkit.entity.LivingEntity) : Damager()
}

class SmashDamageEvent(
    val victim: LivingEntity,
    val damager: Damager?,
    val damage: Double,
    val knockbackMultiplier: Double = 1.0,
    val damageType: SmashDamageType? = null,
) : TwilightEvent() {

    fun isValid(minigame: BrawlMinigame<MinigameDef>): Boolean {
        // TODO: Once we figure out how we want to handle all living entities in minigames, this
        // check will be removed
        if (victim !is Player) {
            return false
        }

        // Check if damager is a player when it's a LivingEntity
        val damagerPlayer =
            when (damager) {
                is Damager.LivingEntity -> damager.livingEntity as? Player
                is Damager.System -> return minigame.isPlayerInMinigame(victim)
                null -> return false
            }

        if (damagerPlayer == null) {
            return false
        }

        return minigame.isPlayerInMinigame(damagerPlayer) && minigame.isPlayerInMinigame(victim)
    }
}
