package dev.betrix.superSmashMobsBrawl.hotbar

import dev.betrix.superSmashMobsBrawl.Manageable
import dev.betrix.superSmashMobsBrawl.SuperSmashMobsBrawl
import dev.betrix.superSmashMobsBrawl.extensions.event
import dev.betrix.superSmashMobsBrawl.services.LangService
import org.bukkit.Material
import org.bukkit.NamespacedKey
import org.bukkit.entity.Player
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.inventory.EquipmentSlot
import org.bukkit.inventory.ItemFlag
import org.bukkit.inventory.ItemStack
import org.bukkit.persistence.PersistentDataType
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

abstract class HotbarItem(
    protected val player: Player,
    protected val itemId: String,
    protected val material: Material,
    protected val slot: Int,
) : Manageable(), KoinComponent {
    protected val plugin: SuperSmashMobsBrawl by inject()
    protected val lang: LangService by inject()

    private val itemKey = NamespacedKey(plugin, "hotbarItemId")
    protected lateinit var itemStack: ItemStack
        private set

    abstract fun onClick()

    protected abstract fun getDisplayName(): String

    protected abstract fun getLore(): List<String>

    override fun setup() {
        itemStack =
            ItemStack.of(material).apply {
                amount = 1
                val meta = itemMeta

                meta.isUnbreakable = true
                meta.addItemFlags(ItemFlag.HIDE_UNBREAKABLE)
                meta.displayName(lang.t(getDisplayName()))

                val loreKeys = this@HotbarItem.getLore()
                if (loreKeys.isNotEmpty()) {
                    meta.lore(loreKeys.map { lang.t(it) })
                }

                meta.persistentDataContainer.set(itemKey, PersistentDataType.STRING, itemId)

                itemMeta = meta
            }

        val currentItem = player.inventory.getItem(slot)
        if (currentItem != null && currentItem.type != Material.AIR) {
            plugin.logger.warning(
                "Hotbar slot $slot was already occupied for player ${player.name}, placing item in first empty slot"
            )
            val emptySlot = player.inventory.firstEmpty()
            if (emptySlot != -1) {
                player.inventory.setItem(emptySlot, itemStack)
            } else {
                plugin.logger.warning(
                    "No empty slots available for player ${player.name}, could not place hotbar item $itemId"
                )
            }
        } else {
            player.inventory.setItem(slot, itemStack)
        }

        listeners.add(event<PlayerInteractEvent>(player) { onPlayerInteract(this) })
    }

    override fun teardown() {
        if (::itemStack.isInitialized) {
            player.inventory.remove(itemStack)
        }

        listeners.forEach { it.unregister() }
        runnables.forEach { it.cancel() }
        jobs.forEach { it.cancel() }
    }

    private fun onPlayerInteract(event: PlayerInteractEvent) {
        if (event.hand != null && event.hand != EquipmentSlot.HAND) return

        val item =
            event.item
                ?: player.inventory.itemInMainHand.takeIf { it.type != Material.AIR }
                ?: return

        if (!isCorrectItemForHotbarItem(item)) {
            return
        }

        event.isCancelled = true
        onClick()
    }

    private fun isCorrectItemForHotbarItem(item: ItemStack): Boolean {
        val itemIdFromStack = getItemId(item) ?: return false
        return itemIdFromStack == itemId
    }

    private fun getItemId(item: ItemStack?): String? {
        if (item == null || !item.hasItemMeta()) return null

        val meta = item.itemMeta ?: return null
        return meta.persistentDataContainer.get(itemKey, PersistentDataType.STRING)
    }
}

