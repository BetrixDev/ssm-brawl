package dev.betrix.superSmashMobsBrawl.services

import dev.betrix.superSmashMobsBrawl.abilities.AbilityUsageType
import dev.betrix.superSmashMobsBrawl.brawl.BrawlAbility
import dev.betrix.superSmashMobsBrawl.brawl.BrawlKit
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

    private val playersWithKits = mutableSetOf<Player>()

    fun initialize(plugin: JavaPlugin) {
        this.plugin = plugin
        this.abilityKey = NamespacedKey(plugin, "ability_id")
        plugin.server.pluginManager.registerEvents(this, plugin)
        repeatingTask(20) { updateAllPlayerHotbars() }
    }

    fun setupHotbarItems(kit: BrawlKit) {
        val player = kit.player
        playersWithKits.add(player)
        clearHotbar(player)
        kit.abilities.forEach { ability -> addAbilityToHotbar(player, ability) }
        player.updateInventory()
    }

    fun clearHotbarItems(player: Player) {
        playersWithKits.remove(player)
        clearHotbar(player)
        player.updateInventory()
    }

    private fun addAbilityToHotbar(player: Player, ability: BrawlAbility) {
        val metadata = ability.metadata
        val slot = metadata.hotbarItemSlot
        val item = metadata.hotbarItem.clone()

        val meta = item.itemMeta
        if (meta != null) {
            meta.persistentDataContainer.set(abilityKey, PersistentDataType.STRING, ability.id)
            val lore = mutableListOf<String>()
            lore.add("§7${metadata.description}")
            if (ability.isOnCooldown()) {
                lore.add("§cCooldown: ${ability.getRemainingCooldown()}s")
            } else {
                lore.add("§aReady to use!")
            }
            lore.add("§eRight-click to activate")
            meta.lore = lore
            item.itemMeta = meta
        }

        player.inventory.setItem(slot, item)
    }

    private fun clearHotbar(player: Player) {
        for (i in 0..8) {
            player.inventory.setItem(i, null)
        }
    }

    fun updateHotbarItems(kit: BrawlKit) {
        val player = kit.player
        if (!playersWithKits.contains(player)) return
        kit.abilities.forEach { ability -> addAbilityToHotbar(player, ability) }
        player.updateInventory()
    }

    private fun getAbilityId(item: ItemStack?): String? {
        if (item == null || !item.hasItemMeta()) return null
        val meta = item.itemMeta ?: return null
        return meta.persistentDataContainer.get(abilityKey, PersistentDataType.STRING)
    }

    @EventHandler
    fun onPlayerInteract(event: PlayerInteractEvent) {
        val player = event.player
        if (!playersWithKits.contains(player)) return
        val item = event.item ?: return
        val abilityId = getAbilityId(item) ?: return

        val kit = KitService.getBrawlKit(player) ?: return
        val ability = kit.abilities.find { it.id == abilityId } ?: return

        when (ability.metadata.usageType) {
            AbilityUsageType.LEFT_CLICK -> {
                if (
                    event.action != Action.LEFT_CLICK_AIR && event.action != Action.LEFT_CLICK_BLOCK
                )
                    return
            }
            AbilityUsageType.RIGHT_CLICK -> {
                if (
                    event.action != Action.RIGHT_CLICK_AIR &&
                        event.action != Action.RIGHT_CLICK_BLOCK
                )
                    return
            }
        }

        event.isCancelled = true

        if (ability.canActivate()) {
            ability.activate()
            updateHotbarItems(kit)
        }
    }

    @EventHandler
    fun onPlayerQuit(event: PlayerQuitEvent) {
        playersWithKits.remove(event.player)
    }

    private fun updateAllPlayerHotbars() {
        playersWithKits.toList().forEach { player ->
            val kit = KitService.getBrawlKit(player)
            if (kit != null) {
                updateHotbarItems(kit)
            } else {
                playersWithKits.remove(player)
            }
        }
    }
}
