package net.ssmb.events

import org.bukkit.entity.LivingEntity

class BrawlDamageEvent(
    val victim: LivingEntity,
    val attacker: LivingEntity,
    val damage: Double,
    val damageType: BrawlDamageType
) : BrawlEvent() {
    var knockbackMultiplier: Double = 1.0
}

enum class BrawlDamageType {
    MELEE,
    PROJECTILE,
    FALL,
    VOID,
    STARVATION,
    SPECIAL,
    UNKNOWN
}
