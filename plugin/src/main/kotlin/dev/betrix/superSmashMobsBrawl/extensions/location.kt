package dev.betrix.superSmashMobsBrawl.extensions

import gg.flyte.twilight.scheduler.delay
import kotlin.math.sqrt
import org.bukkit.*
import org.bukkit.entity.Entity
import org.bukkit.inventory.ItemStack
import org.bukkit.util.Vector

/**
 * Interpolates between this Location and the endLocation, returning a list of Locations.
 *
 * @param endLocation The Location to interpolate to.
 * @param spacing The distance between each interpolated point. (must be > 0)
 */
fun Location.interpolateTo(endLocation: Location, spacing: Double): List<Location> {
    require(spacing > 0) { "spacing must be greater than 0, was $spacing" }
    require(endLocation.world == world) {
        "start and end locations must be in the same world (start=$world, end=${endLocation.world})"
    }

    val locations = mutableListOf<Location>()

    val dx = endLocation.x - x
    val dy = endLocation.y - y
    val dz = endLocation.z - z

    val distance = sqrt(dx * dx + dy * dy + dz * dz)

    // Ensure at least 2 points (start and end)
    val points = maxOf(2, (distance / spacing).toInt() + 1)

    val stepX = dx / (points - 1)
    val stepY = dy / (points - 1)
    val stepZ = dz / (points - 1)

    for (i in 0 until points) {
        val px = x + stepX * i
        val py = y + stepY * i
        val pz = z + stepZ * i
        locations.add(Location(world, px, py, pz))
    }

    return locations
}

fun Location.itemEffect(
    itemCount: Int,
    velocity: Double,
    sound: Sound?,
    soundVolume: Float,
    soundPitch: Float,
    material: Material?,
    debrisLifetimeTicks: Long,
) {
    if (material == null || material == Material.AIR) {
        return
    }

    val items = arrayListOf<Entity>()
    val itemStack = ItemStack.of(material)

    repeat(itemCount) { idx ->
        val item = world.dropItem(this, itemStack)
        item.velocity =
            Vector(
                (Math.random() - 0.5) * velocity,
                Math.random() * velocity,
                (Math.random() - 0.5) * velocity,
            )
        item.pickupDelay = 999999
        items.add(item)
    }

    if (sound != null) {
        world.playSound(this, sound, soundVolume, soundPitch)
    }

    delay(debrisLifetimeTicks) {
        items.forEach { it.remove() }

        items.clear()
    }
}
