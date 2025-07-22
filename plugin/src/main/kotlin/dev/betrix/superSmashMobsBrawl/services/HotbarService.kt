package dev.betrix.superSmashMobsBrawl.services

import dev.betrix.superSmashMobsBrawl.abilities.instances.AbilityInstance
import dev.betrix.superSmashMobsBrawl.kits.instances.KitInstance
import gg.flyte.twilight.event.event
import gg.flyte.twilight.scheduler.repeatingTask
import org.bukkit.NamespacedKey
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.block.Action
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.event.player.PlayerQuitEvent
import org.bukkit.inventory.ItemStack
import org.bukkit.persistence.PersistentDataType
import org.bukkit.plugin.java.JavaPlugin

object HotbarService : Listener {
    private lateinit var plugin: JavaPlugin
    private lateinit var abilityKey: NamespacedKey
    
    // Track which players have kits with hotbar items
    private val playersWithKits = mutableSetOf<Player>()
    
    fun initialize(plugin: JavaPlugin) {
        this.plugin = plugin
        this.abilityKey = NamespacedKey(plugin, "ability_id")
        plugin.server.pluginManager.registerEvents(this, plugin)
        
        // Start periodic hotbar updates (every 1 second)
        repeatingTask(20) {
            updateAllPlayerHotbars()
        }
    }
    
    /**
     * Sets up hotbar items for a kit instance
     */
    fun setupHotbarItems(kitInstance: KitInstance) {
        val player = kitInstance.player
        playersWithKits.add(player)
        
        // Clear hotbar first
        clearHotbar(player)
        
        // Add ability items to hotbar
        kitInstance.abilityInstances.forEach { abilityInstance ->
            addAbilityToHotbar(player, abilityInstance)
        }
        
        player.updateInventory()
    }
    
    /**
     * Clears hotbar items for a player when their kit is removed
     */
    fun clearHotbarItems(player: Player) {
        playersWithKits.remove(player)
        clearHotbar(player)
        player.updateInventory()
    }
    
    /**
     * Adds an ability item to the player's hotbar
     */
    private fun addAbilityToHotbar(player: Player, abilityInstance: AbilityInstance) {
        val metadata = abilityInstance.definition.metadata
        val slot = metadata.hotbarItemSlot
        
        // Clone the item to avoid modifying the original
        val item = metadata.hotbarItem.clone()
        
        // Add persistent data to identify which ability this item represents
        val meta = item.itemMeta
        if (meta != null) {
            meta.persistentDataContainer.set(abilityKey, PersistentDataType.STRING, abilityInstance.definition.id)
            
            // Add cooldown info to lore if on cooldown
            val lore = mutableListOf<String>()
            lore.add("§7${metadata.description}")
            if (abilityInstance.isOnCooldown()) {
                lore.add("§cCooldown: ${abilityInstance.getRemainingCooldown()}s")
            } else {
                lore.add("§aReady to use!")
            }
            lore.add("§eRight-click to activate")
            
            meta.lore = lore
            item.itemMeta = meta
        }
        
        // Place item in the specified slot
        player.inventory.setItem(slot, item)
    }
    
    /**
     * Clears the player's hotbar (slots 0-8)
     */
    private fun clearHotbar(player: Player) {
        for (i in 0..8) {
            player.inventory.setItem(i, null)
        }
    }
    
    /**
     * Updates hotbar items to reflect current cooldown states
     */
    fun updateHotbarItems(kitInstance: KitInstance) {
        val player = kitInstance.player
        if (!playersWithKits.contains(player)) return
        
        kitInstance.abilityInstances.forEach { abilityInstance ->
            addAbilityToHotbar(player, abilityInstance)
        }
        
        player.updateInventory()
    }
    
    /**
     * Gets the ability ID from an item's persistent data
     */
    private fun getAbilityId(item: ItemStack?): String? {
        if (item == null || !item.hasItemMeta()) return null
        
        val meta = item.itemMeta ?: return null
        return meta.persistentDataContainer.get(abilityKey, PersistentDataType.STRING)
    }
    
    /**
     * Finds a kit instance for a player
     */
    private fun getKitInstance(player: Player): KitInstance? {
        // We need to get this from wherever kit instances are stored
        // For now, let's add a method to get it from KitService
        return KitService.getKitInstance(player)
    }
    
    @EventHandler
    fun onPlayerInteract(event: PlayerInteractEvent) {
        val player = event.player
        
        // Only handle right-click actions
        if (event.action != Action.RIGHT_CLICK_AIR && event.action != Action.RIGHT_CLICK_BLOCK) {
            return
        }
        
        // Only handle players with kits
        if (!playersWithKits.contains(player)) {
            return
        }
        
        val item = event.item ?: return
        val abilityId = getAbilityId(item) ?: return
        
        // Cancel the event to prevent normal item behavior
        event.isCancelled = true
        
        // Find the kit instance and corresponding ability
        val kitInstance = getKitInstance(player) ?: return
        val abilityInstance = kitInstance.abilityInstances.find { it.definition.id == abilityId } ?: return
        
        // Attempt to activate the ability
        val success = abilityInstance.activate()
        
        // Update hotbar items to reflect new cooldown state
        if (success) {
            updateHotbarItems(kitInstance)
        }
    }
    
    @EventHandler
    fun onPlayerQuit(event: PlayerQuitEvent) {
        // Clean up tracking when player disconnects
        playersWithKits.remove(event.player)
    }
    
    /**
     * Updates hotbar items for all players with kits
     */
    private fun updateAllPlayerHotbars() {
        playersWithKits.toList().forEach { player ->
            val kitInstance = getKitInstance(player)
            if (kitInstance != null) {
                updateHotbarItems(kitInstance)
            } else {
                // Player no longer has a kit, remove from tracking
                playersWithKits.remove(player)
            }
        }
    }
}