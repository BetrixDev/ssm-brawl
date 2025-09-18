package dev.betrix.superSmashMobsBrawl.events

import gg.flyte.twilight.event.TwilightEvent
import org.bukkit.entity.LivingEntity

sealed class BrawlDamageType {
    data object Projectile : BrawlDamageType()

    data object Explosion : BrawlDamageType()

    data object MeleeAttack : BrawlDamageType()
}

sealed class Damager {
    data object System : Damager()

    data class DamagerLivingEntity(val livingEntity: LivingEntity) : Damager()
}

class BrawlDamageEvent(
    val victim: LivingEntity,
    val damager: Damager?,
    var damage: Double,
    var knockbackMultiplier: Double = 1.0,
    val damageType: BrawlDamageType? = null,
) : TwilightEvent()
