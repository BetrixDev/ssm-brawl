package dev.betrix.superSmashMobsBrawl.extensions

import java.util.WeakHashMap
import org.bukkit.inventory.Inventory

private val brawlInventories = WeakHashMap<Inventory, Boolean>()

var Inventory.isBrawlInventory: Boolean
    get() = brawlInventories[this] ?: false
    set(value) {
        if (value) {
            brawlInventories[this] = true
        } else {
            brawlInventories.remove(this)
        }
    }
