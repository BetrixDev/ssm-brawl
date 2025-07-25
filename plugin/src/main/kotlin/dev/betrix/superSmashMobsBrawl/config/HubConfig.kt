package dev.betrix.superSmashMobsBrawl.config

import org.bukkit.Location
import org.bukkit.World
import org.bukkit.configuration.file.FileConfiguration
import org.bukkit.plugin.java.JavaPlugin

/**
 * Configuration manager for hub-related settings
 */
object HubConfig {
    private lateinit var plugin: JavaPlugin
    private lateinit var config: FileConfiguration
    
    /**
     * Initialize the hub configuration
     */
    fun initialize(plugin: JavaPlugin) {
        this.plugin = plugin
        this.config = plugin.config
        
        // Set default values if they don't exist
        setDefaults()
        
        // Save the config
        plugin.saveConfig()
    }
    
    /**
     * Set default configuration values
     */
    private fun setDefaults() {
        if (!config.contains("hub.default-world")) {
            config.set("hub.default-world", "blue_forest")
        }
        
        if (!config.contains("hub.spawn.x")) {
            config.set("hub.spawn.x", 0.0)
        }
        
        if (!config.contains("hub.spawn.y")) {
            config.set("hub.spawn.y", 100.0)
        }
        
        if (!config.contains("hub.spawn.z")) {
            config.set("hub.spawn.z", 0.0)
        }
        
        if (!config.contains("hub.spawn.yaw")) {
            config.set("hub.spawn.yaw", 0.0f)
        }
        
        if (!config.contains("hub.spawn.pitch")) {
            config.set("hub.spawn.pitch", 0.0f)
        }
        
        if (!config.contains("hub.enable-double-jump")) {
            config.set("hub.enable-double-jump", true)
        }
        
        if (!config.contains("hub.protection.enabled")) {
            config.set("hub.protection.enabled", true)
        }
        
        if (!config.contains("hub.protection.prevent-damage")) {
            config.set("hub.protection.prevent-damage", true)
        }
        
        if (!config.contains("hub.protection.prevent-block-break")) {
            config.set("hub.protection.prevent-block-break", true)
        }
        
        if (!config.contains("hub.protection.prevent-block-place")) {
            config.set("hub.protection.prevent-block-place", true)
        }
        
        if (!config.contains("hub.protection.prevent-item-pickup")) {
            config.set("hub.protection.prevent-item-pickup", true)
        }
        
        if (!config.contains("hub.protection.prevent-weather")) {
            config.set("hub.protection.prevent-weather", true)
        }
    }
    
    /**
     * Get the default hub world name
     */
    fun getDefaultWorldName(): String {
        return config.getString("hub.default-world", "blue_forest")!!
    }
    
    /**
     * Get the hub spawn location for a given world
     */
    fun getSpawnLocation(world: World): Location {
        val x = config.getDouble("hub.spawn.x", 0.0)
        val y = config.getDouble("hub.spawn.y", 100.0)
        val z = config.getDouble("hub.spawn.z", 0.0)
        val yaw = config.getDouble("hub.spawn.yaw", 0.0).toFloat()
        val pitch = config.getDouble("hub.spawn.pitch", 0.0).toFloat()
        
        return Location(world, x, y, z, yaw, pitch)
    }
    
    /**
     * Check if double jump is enabled in hubs
     */
    fun isDoubleJumpEnabled(): Boolean {
        return config.getBoolean("hub.enable-double-jump", true)
    }
    
    /**
     * Check if hub protection is enabled
     */
    fun isProtectionEnabled(): Boolean {
        return config.getBoolean("hub.protection.enabled", true)
    }
    
    /**
     * Check if damage protection is enabled
     */
    fun isDamageProtectionEnabled(): Boolean {
        return config.getBoolean("hub.protection.prevent-damage", true)
    }
    
    /**
     * Check if block break protection is enabled
     */
    fun isBlockBreakProtectionEnabled(): Boolean {
        return config.getBoolean("hub.protection.prevent-block-break", true)
    }
    
    /**
     * Check if block place protection is enabled
     */
    fun isBlockPlaceProtectionEnabled(): Boolean {
        return config.getBoolean("hub.protection.prevent-block-place", true)
    }
    
    /**
     * Check if item pickup protection is enabled
     */
    fun isItemPickupProtectionEnabled(): Boolean {
        return config.getBoolean("hub.protection.prevent-item-pickup", true)
    }
    
    /**
     * Check if weather protection is enabled
     */
    fun isWeatherProtectionEnabled(): Boolean {
        return config.getBoolean("hub.protection.prevent-weather", true)
    }
    
    /**
     * Reload the configuration
     */
    fun reload() {
        plugin.reloadConfig()
        this.config = plugin.config
        setDefaults()
        plugin.saveConfig()
    }
}