package dev.betrix.superSmashMobsBrawl.extensions

import gg.flyte.twilight.scheduler.delay
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.Sound
import org.bukkit.entity.Entity
import org.bukkit.inventory.ItemStack
import org.bukkit.util.Vector

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
