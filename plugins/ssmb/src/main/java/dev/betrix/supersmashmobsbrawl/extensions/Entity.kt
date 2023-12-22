package dev.betrix.supersmashmobsbrawl.extensions

import dev.betrix.supersmashmobsbrawl.SuperSmashMobsBrawl
import dev.betrix.supersmashmobsbrawl.enums.TaggedKeyBool
import dev.betrix.supersmashmobsbrawl.enums.TaggedKeyNum
import dev.betrix.supersmashmobsbrawl.enums.TaggedKeyStr
import dev.betrix.supersmashmobsbrawl.utils.isOnGround
import org.bukkit.attribute.Attribute
import org.bukkit.entity.Entity
import org.bukkit.entity.LivingEntity
import org.bukkit.entity.Player
import org.bukkit.entity.Projectile
import org.bukkit.metadata.FixedMetadataValue
import org.bukkit.util.Vector
import kotlin.math.abs
import kotlin.math.log10

class EntityMetadataBuilder(private val entity: Entity) {
    private val metadata = mutableMapOf<String, Any>()
    private val metadataToRemove = arrayListOf<String>()

    fun set(metadataKey: TaggedKeyNum, value: Double) {
        metadata[metadataKey.id] = value
    }

    fun set(metadataKey: TaggedKeyStr, value: String) {
        metadata[metadataKey.id] = value
    }

    fun set(metadataKey: TaggedKeyBool, value: Boolean) {
        metadata[metadataKey.id] = value
    }

    fun remove(metadataKey: TaggedKeyNum) {
        metadataToRemove.add(metadataKey.id)
    }

    fun remove(metadataKey: TaggedKeyStr) {
        metadataToRemove.add(metadataKey.id)
    }

    fun remove(metadataKey: TaggedKeyBool) {
        metadataToRemove.add(metadataKey.id)
    }

    fun build() {
        val plugin = SuperSmashMobsBrawl.instance

        metadata.forEach {
            entity.setMetadata(it.key, FixedMetadataValue(plugin, it.value))
        }

        metadataToRemove.forEach {
            entity.removeMetadata(it, plugin)
        }
    }
}

fun Entity.metadata(builder: EntityMetadataBuilder.() -> Unit) {
    val metadataBuilder = EntityMetadataBuilder(this)
    builder(metadataBuilder)
    metadataBuilder.build()
}

fun Entity.getMetadata(metadataKey: TaggedKeyNum): Double? {
    return this.getMetadata(metadataKey.id).getOrNull(0)?.value() as Double?
}

fun Entity.getMetadata(metadataKey: TaggedKeyStr): String? {
    val value = this.getMetadata(metadataKey.id).getOrNull(0) ?: return null
    return value.value().toString()
}

fun Entity.getMetadata(metadataKey: TaggedKeyBool): Boolean? {
    val value = this.getMetadata(metadataKey.id).getOrNull(0) ?: return null
    return value.value().toString().toBoolean()
}

fun Entity.setVelocity(strength: Double, yAdd: Double, yMax: Double, groundBoost: Boolean) {
    setVelocity(this.location.direction, strength, false, 0.0, yAdd, yMax, groundBoost)
}

fun Entity.setVelocity(
    velocity: Vector,
    strength: Double,
    ySet: Boolean,
    yBase: Double,
    yAdd: Double,
    yMax: Double,
    groundBoost: Boolean
) {
    if (velocity.x.isNaN() || velocity.y.isNaN() || velocity.z.isNaN() || velocity.length() == 0.0) {
        return
    }

    if (ySet) {
        velocity.y = yBase
    }

    velocity.normalize()
    velocity.multiply(strength)

    velocity.y += yAdd

    if (velocity.y > yMax) {
        velocity.y = yMax
    }

    if (groundBoost) {
        if (isOnGround(this)) {
            velocity.y += 0.2
        }
    }

    this.fallDistance = 0F

    this.velocity = velocity
}

fun Entity.doKnockback(
    multiplier: Double,
    damage: Double,
    startingHealth: Double,
    origin: Vector?,
    projectile: Projectile?
) {
    this.velocity = Vector(0.0, 0.0, 0.0)
    var knockback = damage.coerceAtLeast(2.0)
    knockback = log10(knockback)
    knockback *= multiplier

    if (this is Player) {
        val kitKnockbackMult = this.getMetadata(TaggedKeyNum.PLAYER_KIT_KNOCKBACK_MULT)

        if (kitKnockbackMult != null) {
            knockback *= kitKnockbackMult
        }

        knockback *= (1 + 0.1 * (this.getAttribute(Attribute.GENERIC_MAX_HEALTH)?.value!! - startingHealth))
    }

    var trajectory: Vector? = null
    if (origin != null) {
        trajectory = this.location.toVector().subtract(origin).setY(0).normalize()
        trajectory.multiply(0.6 * knockback)
        trajectory.y = abs(trajectory.y)
    }
    if (projectile != null) {
        trajectory = projectile.velocity
        trajectory.y = 0.0
        trajectory.multiply(0.37 * knockback / trajectory.length())
        trajectory.y = 0.06
    }

    val vel = 0.2 + trajectory!!.length() * 0.8

    this.setVelocity(trajectory, vel, false, 0.0, abs(0.2 * knockback), 0.4 + (0.04 * knockback), true)
}

fun Entity.getLivingEntitiesInRadius(radius: Double): List<LivingEntity> {
    return this.getNearbyEntities(radius, radius, radius).filterIsInstance<LivingEntity>()
}