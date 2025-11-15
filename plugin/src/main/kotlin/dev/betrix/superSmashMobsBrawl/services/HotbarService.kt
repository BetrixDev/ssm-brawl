package dev.betrix.superSmashMobsBrawl.services

import dev.betrix.superSmashMobsBrawl.Manageable
import dev.betrix.superSmashMobsBrawl.extensions.event
import dev.betrix.superSmashMobsBrawl.hotbar.HotbarItem
import dev.betrix.superSmashMobsBrawl.hotbar.HotbarPreset
import gg.flyte.twilight.event.event
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap
import java.util.logging.Logger
import org.bukkit.entity.Player
import org.bukkit.event.player.PlayerQuitEvent
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

object HotbarService : Manageable(), KoinComponent {
    private val logger: Logger by inject()
    private val kitService: KitService by inject()
    private val minigameService: MinigameService by inject()

    private val activeHotbarItems = ConcurrentHashMap<UUID, List<HotbarItem>>()

    override fun setup() {
        listeners.add(
            event<PlayerQuitEvent> {
                clearHotbar(player)
            }
        )
    }

    fun applyPreset(player: Player, preset: HotbarPreset) {
        // Clear any existing hotbar items first
        clearHotbar(player)

        // Create and setup new items from preset
        val items = preset.apply(player)

        items.forEach { item ->
            try {
                item.setup()
            } catch (e: Exception) {
                logger.warning(
                    "Failed to setup hotbar item for player ${player.name}: ${e.message}"
                )
                e.printStackTrace()
            }
        }

        activeHotbarItems[player.uniqueId] = items
    }

    fun clearHotbar(player: Player) {
        val items = activeHotbarItems.remove(player.uniqueId)
        items?.forEach { item ->
            try {
                item.teardown()
            } catch (e: Exception) {
                logger.warning(
                    "Failed to teardown hotbar item for player ${player.name}: ${e.message}"
                )
                e.printStackTrace()
            }
        }
    }

    fun isHotbarControlled(player: Player): Boolean {
        // Check if kit service has assigned a combat kit to the player
        if (kitService.hasKit(player)) {
            return true
        }

        // Check if player is in an active minigame
        if (minigameService.isPlayerInMinigame(player)) {
            return true
        }

        // Check if we have active hotbar items for this player
        if (activeHotbarItems.containsKey(player.uniqueId)) {
            return true
        }

        return false
    }

    override fun teardown() {
        // Clean up all active hotbar items
        activeHotbarItems.keys.toList().forEach { uuid ->
            val player = org.bukkit.Bukkit.getPlayer(uuid)
            if (player != null) {
                clearHotbar(player)
            }
        }

        activeHotbarItems.clear()

        // Clean up listeners, runnables, and jobs
        listeners.forEach { it.unregister() }
        runnables.forEach { it.cancel() }
        jobs.forEach { it.cancel() }

        listeners.clear()
        runnables.clear()
        jobs.clear()
    }
}

