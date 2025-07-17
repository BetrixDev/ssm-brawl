package dev.betrix.superSmashMobsBrawl.kits.instances

import dev.betrix.superSmashMobsBrawl.kits.definitions.KitDefinition
import org.bukkit.entity.Player

abstract class KitInstance(
    val definition: KitDefinition,
    val player: Player
) {
    abstract fun setup(): Unit

    abstract fun teardown(): Unit
}