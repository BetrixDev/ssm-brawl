package dev.betrix.superSmashMobsBrawl.systems

import com.github.quillraven.fleks.Entity
import com.github.quillraven.fleks.IteratingSystem
import com.github.quillraven.fleks.World.Companion.family
import com.github.quillraven.fleks.World.Companion.inject
import dev.betrix.superSmashMobsBrawl.components.MinigameComponent
import dev.betrix.superSmashMobsBrawl.components.MinigameState
import dev.betrix.superSmashMobsBrawl.components.PlayerComponent
import dev.betrix.superSmashMobsBrawl.extensions.getEquidistant
import dev.betrix.superSmashMobsBrawl.extensions.location
import dev.betrix.superSmashMobsBrawl.models.BrawlGameWorld
import dev.betrix.superSmashMobsBrawl.services.KitService
import org.bukkit.Material
import org.bukkit.inventory.ItemStack

class MinigameStartSystem(private val kitService: KitService = inject()) :
    IteratingSystem(family { all(MinigameComponent) }) {

    override fun onTickEntity(entity: Entity) {
        val minigame = entity[MinigameComponent]
        if (minigame.state != MinigameState.STARTING) return

        if (!minigame.hasLoadedWorld()) return
        val world = minigame.loadedWorld as? BrawlGameWorld ?: return

        // Teleport players and assign kits
        val spawnPoints = world.data.spawnPoints.getEquidistant(minigame.playerEntities.size)

        minigame.playerEntities.forEachIndexed { idx, playerEntity ->
            val player = playerEntity[PlayerComponent].player
            val sp = spawnPoints[idx.coerceAtMost(spawnPoints.lastIndex)]
            player.teleport(world.world.location(sp))

            // Ensure no duplicate assignment by unassigning first, then assigning
            kitService.unassignKit(player)
            kitService.assignKit(player)

            // Clear offhand to remove 1.9+ shield mechanics
            try {
                player.inventory.setItemInOffHand(ItemStack.of(Material.AIR))
            } catch (_: Throwable) {}
        }

        // Advance to ONGOING, remaining logic (damage/respawn/etc.) is handled by other systems
        minigame.state = MinigameState.ONGOING
    }
}
