package dev.betrix.superSmashMobsBrawl.services

import dev.betrix.superSmashMobsBrawl.config.HubConfig
import gg.flyte.twilight.event.event
import org.bukkit.GameMode
import org.bukkit.entity.Player
import org.bukkit.event.block.BlockBreakEvent
import org.bukkit.event.block.BlockPlaceEvent
import org.bukkit.event.entity.EntityDamageByEntityEvent
import org.bukkit.event.entity.EntityDamageEvent
import org.bukkit.event.entity.EntityTargetEvent
import org.bukkit.event.entity.FoodLevelChangeEvent
import org.bukkit.event.player.PlayerInteractEntityEvent
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.event.player.PlayerPickupArrowEvent
import org.bukkit.event.player.PlayerPickupItemEvent
import org.bukkit.event.weather.WeatherChangeEvent

/**
 * Service responsible for protecting players and the environment in hub worlds
 */
object HubProtectionService {
    
    /**
     * Register all protection events
     */
    fun registerEvents() {
        // Block protection
        event<BlockBreakEvent> {
            if (HubService.isInHub(player, block.world) && HubConfig.isBlockBreakProtectionEnabled()) {
                if (player.gameMode != GameMode.CREATIVE) {
                    isCancelled = true
                }
            }
        }
        
        event<BlockPlaceEvent> {
            if (HubService.isInHub(player, block.world) && HubConfig.isBlockPlaceProtectionEnabled()) {
                if (player.gameMode != GameMode.CREATIVE) {
                    isCancelled = true
                }
            }
        }
        
        // Player interaction protection
        event<PlayerInteractEvent> {
            if (HubService.isInHub(player, player.world)) {
                // Allow certain interactions but prevent block breaking/placing
                when (action) {
                    org.bukkit.event.block.Action.LEFT_CLICK_BLOCK -> {
                        if (player.gameMode != GameMode.CREATIVE && HubConfig.isBlockBreakProtectionEnabled()) {
                            isCancelled = true
                        }
                    }
                    org.bukkit.event.block.Action.RIGHT_CLICK_BLOCK -> {
                        // Allow right-click for doors, buttons, etc. but prevent block placement
                        if (player.gameMode != GameMode.CREATIVE && clickedBlock != null && HubConfig.isBlockPlaceProtectionEnabled()) {
                            // Check if it's a block that can be placed
                            val itemInHand = player.inventory.itemInMainHand
                            if (itemInHand.type.isBlock) {
                                isCancelled = true
                            }
                        }
                    }
                    else -> { /* Allow other interactions */ }
                }
            }
        }
        
        // Entity interaction protection
        event<PlayerInteractEntityEvent> {
            if (HubService.isInHub(player, player.world)) {
                // Allow interaction with entities but prevent damage
                // This allows for NPCs, signs, etc. to work
            }
        }
        
        // Damage protection (comprehensive)
        event<EntityDamageEvent> {
            if (entity is Player && HubConfig.isDamageProtectionEnabled()) {
                val player = entity as Player
                if (HubService.isInHub(player, player.world)) {
                    isCancelled = true
                }
            }
        }
        
        event<EntityDamageByEntityEvent> {
            if (HubConfig.isDamageProtectionEnabled()) {
                if (damager is Player) {
                    val player = damager as Player
                    if (HubService.isInHub(player, player.world)) {
                        isCancelled = true
                    }
                }
                if (entity is Player) {
                    val player = entity as Player
                    if (HubService.isInHub(player, player.world)) {
                        isCancelled = true
                    }
                }
            }
        }
        
        // Food level protection
        event<FoodLevelChangeEvent> {
            if (entity is Player) {
                val player = entity as Player
                if (HubService.isInHub(player, player.world)) {
                    isCancelled = true
                }
            }
        }
        
        // Entity targeting protection
        event<EntityTargetEvent> {
            if (target is Player) {
                val player = target as Player
                if (HubService.isInHub(player, player.world)) {
                    isCancelled = true
                }
            }
        }
        
        // Item pickup protection (prevent accidental pickups)
        event<PlayerPickupItemEvent> {
            if (HubService.isInHub(player, player.world) && HubConfig.isItemPickupProtectionEnabled()) {
                isCancelled = true
            }
        }
        
        event<PlayerPickupArrowEvent> {
            if (HubService.isInHub(player, player.world) && HubConfig.isItemPickupProtectionEnabled()) {
                isCancelled = true
                }
        }
        
        // Weather protection (keep hub worlds nice)
        event<WeatherChangeEvent> {
            if (HubService.isInHub(null, world) && HubConfig.isWeatherProtectionEnabled()) {
                if (toWeatherState()) {
                    isCancelled = true
                }
            }
        }
    }
}