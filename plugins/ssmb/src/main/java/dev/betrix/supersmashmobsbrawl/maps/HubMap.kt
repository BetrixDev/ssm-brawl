package dev.betrix.supersmashmobsbrawl.maps

import net.kyori.adventure.text.minimessage.MiniMessage
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack

class HubMap constructor(
    serverName: String,
    mapId: String
) : BaseMap(serverName, mapId) {
    override fun afterPlayerTeleport(player: Player) {
        val actionItem = ItemStack(Material.COMPASS)
        val actionItemMeta = actionItem.itemMeta
        actionItemMeta.displayName(MiniMessage.miniMessage().deserialize("<aqua>Start a Game"))
        actionItem.itemMeta = actionItemMeta

        player.inventory.clear()
        player.inventory.setItem(4, actionItem)
        player.updateInventory()
        player.inventory.heldItemSlot = 4
    }
}