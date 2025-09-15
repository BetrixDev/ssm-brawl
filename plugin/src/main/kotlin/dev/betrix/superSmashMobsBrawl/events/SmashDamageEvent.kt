package dev.betrix.superSmashMobsBrawl.events

import gg.flyte.twilight.event.TwilightEvent
import org.bukkit.entity.LivingEntity

sealed class SmashDamageType {
    data object Projectile : SmashDamageType()

    data object Explosion : SmashDamageType()

    data object MeleeAttack : SmashDamageType()
}

sealed class Damager {
    data object System : Damager()

    data class DamagerLivingEntity(val livingEntity: LivingEntity) : Damager()
}

class SmashDamageEvent(
    val victim: LivingEntity,
    val damager: Damager?,
    var damage: Double,
    var knockbackMultiplier: Double = 1.0,
    val damageType: SmashDamageType? = null,
) : TwilightEvent()
