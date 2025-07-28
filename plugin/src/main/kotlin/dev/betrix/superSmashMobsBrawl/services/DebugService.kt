package dev.betrix.superSmashMobsBrawl.services

import dev.betrix.superSmashMobsBrawl.di.Injectable
import dev.betrix.superSmashMobsBrawl.lifecycle.Manageable
import gg.flyte.twilight.event.event
import org.bukkit.entity.Player
import org.bukkit.event.player.PlayerQuitEvent
import org.bukkit.plugin.java.JavaPlugin
import org.koin.core.component.inject

/**
 * Service responsible for managing debug mode state for players.
 * 
 * This service provides a simple debug toggle system that allows players with the 'ssmb.debug' permission
 * to enable/disable debug mode using the `/debug` command. When debug mode is enabled, developers can
 * show additional debug information to these players anywhere in the codebase.
 * 
 * ## Usage for Developers:
 * 
 * ### Option 1: Using the extension function (recommended)
 * ```kotlin
 * import dev.betrix.superSmashMobsBrawl.extensions.hasDebugEnabled
 * 
 * if (player.hasDebugEnabled()) {
 *     player.sendMessage(mm("<gray>[DEBUG] Some debug information</gray>"))
 * }
 * ```
 * 
 * ### Option 2: Using the service directly
 * ```kotlin
 * if (DebugService.isDebugEnabled(player)) {
 *     player.sendMessage("Debug info here")
 * }
 * ```
 * 
 * ## Player Usage:
 * 1. Player must have 'ssmb.debug' permission
 * 2. Use `/debug` command to toggle debug mode on/off
 * 3. Debug state is automatically cleaned up when player quits
 */
object DebugService : Manageable, Injectable {
    private val plugin: JavaPlugin by inject()
    private val playersWithDebug = mutableSetOf<Player>()

    /** Initialize the debug service */
    fun initialize(plugin: JavaPlugin) {
        registerEvents()
    }

    /** Register debug-related events */
    private fun registerEvents() {
        // Clean up debug state when player quits
        event<PlayerQuitEvent> {
            playersWithDebug.remove(player)
        }
    }

    /** Toggle debug mode for a player */
    fun toggleDebug(player: Player): Boolean {
        return if (playersWithDebug.contains(player)) {
            playersWithDebug.remove(player)
            plugin.logger.info("Debug mode disabled for player: ${player.name}")
            false
        } else {
            playersWithDebug.add(player)
            plugin.logger.info("Debug mode enabled for player: ${player.name}")
            true
        }
    }

    /** Check if a player has debug mode enabled */
    fun isDebugEnabled(player: Player): Boolean {
        return playersWithDebug.contains(player)
    }

    /** Enable debug mode for a player */
    fun enableDebug(player: Player) {
        if (!playersWithDebug.contains(player)) {
            playersWithDebug.add(player)
            plugin.logger.info("Debug mode enabled for player: ${player.name}")
        }
    }

    /** Disable debug mode for a player */
    fun disableDebug(player: Player) {
        if (playersWithDebug.contains(player)) {
            playersWithDebug.remove(player)
            plugin.logger.info("Debug mode disabled for player: ${player.name}")
        }
    }

    /** Clean up debug service */
    override fun teardown() {
        playersWithDebug.clear()
        plugin.logger.info("Debug service cleaned up")
    }
}