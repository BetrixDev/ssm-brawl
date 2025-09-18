package dev.betrix.superSmashMobsBrawl.extensions

import org.bukkit.inventory.Inventory
import java.util.WeakHashMap

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