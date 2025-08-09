package dev.betrix.superSmashMobsBrawl.utils

import org.bukkit.Material
import org.bukkit.inventory.ItemStack

fun itemFromString(id: String, default: Material = Material.AIR): ItemStack {
    val normalized = id.trim()
    val mat = Material.matchMaterial(normalized)
        ?: Material.getMaterial(normalized.uppercase(java.util.Locale.ROOT))
        ?: run {
            println("[items] Unknown material id: '$id', defaulting to $default")
            default
        }
    return org.bukkit.inventory.ItemStack(mat)
}