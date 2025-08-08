package dev.betrix.superSmashMobsBrawl.utils

import org.bukkit.Material
import org.bukkit.inventory.ItemStack

fun itemFromString(id: String, default: Material = Material.STONE): ItemStack {
    val mat = Material.values().find { it.name.equals(id, ignoreCase = true) } ?: default
    return ItemStack.of(mat)
}
