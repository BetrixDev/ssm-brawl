package dev.betrix.superSmashMobsBrawl.extensions

import dev.betrix.superSmashMobsBrawl.models.SpawnPoint
import gg.flyte.twilight.extension.add
import kotlin.math.abs
import kotlin.math.log10
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.entity.Entity
import org.bukkit.entity.Projectile
import org.bukkit.util.Vector

fun Entity.teleport(spawnPoint: SpawnPoint) {
    teleport(Location(world, spawnPoint.x, spawnPoint.y, spawnPoint.z))
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
    groundBoost: Boolean,
) {
    if (
        velocity.x.isNaN() || velocity.y.isNaN() || velocity.z.isNaN() || velocity.length() == 0.0
    ) {
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

    if (groundBoost && isOnGround) {
        velocity.y += 0.2
    }

    this.fallDistance = 0F

    this.velocity = velocity
}

fun Entity.doKnockback(
    multiplier: Double,
    damage: Double,
    startingHealth: Double,
    origin: Vector?,
    projectile: Projectile?,
) {
    this.velocity = Vector(0.0, 0.0, 0.0)
    var knockback = damage.coerceAtLeast(2.0)
    knockback = log10(knockback)
    knockback *= multiplier

    //    if (this is Player) {
    //        val kitKnockbackMult = this.getMetadata(TaggedKeyDouble("knockback_multiplier"))

    //        if (kitKnockbackMult != null) {
    //            knockback *= kitKnockbackMult
    //        }

    //        knockback *=
    //            (1 + 0.1 * (this.getAttribute(Attribute.GENERIC_MAX_HEALTH)?.value!! -
    // startingHealth))
    //    }

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

    this.setVelocity(
        trajectory,
        vel,
        false,
        0.0,
        abs(0.2 * knockback),
        0.4 + (0.04 * knockback),
        true,
    )
}

fun Entity.isOnBlock(): Boolean {
    var xMod = location.x % 1

    if (location.x < 0) {
        xMod += 1
    }

    var zMod = location.z % 1

    if (location.z < 1) {
        zMod += 1
    }

    var xMin = 0
    var xMax = 0
    var zMin = 0
    var zMax = 0

    if (xMod < 0.3) {
        xMin = -1
    }
    if (xMod > 0.7) {
        xMax = 1
    }
    if (zMod < 0.3) {
        zMin = -1
    }
    if (zMod > 0.7) {
        zMax = 1
    }

    for (x in xMin..xMax) {
        for (z in zMin..zMax) {
            if (
                location.add(x, -0.5, z).block.type != Material.AIR &&
                    !location.add(x, -0.5, z).block.isLiquid
            )
                return true
            if (location.add(x, 0, z).block.type == Material.LILY_PAD) return true
            val beneath: Material = location.add(x, -1.5, z).block.type
            if (
                location.y % 0.5 == 0.0 &&
                    (beneath.toString().contains("FENCE") || beneath == Material.COBBLESTONE_WALL)
            )
                return true
        }
    }
    return false
}
