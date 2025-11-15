package dev.betrix.superSmashMobsBrawl.hotbar

import dev.betrix.superSmashMobsBrawl.services.QueueService
import org.bukkit.Material
import org.bukkit.entity.Player
import org.koin.core.component.inject

class HubQueueItem(player: Player) :
    HotbarItem(player, "hub_queue_items", Material.RECOVERY_COMPASS, 4) {

    private val queueService: QueueService by inject()

    override fun onClick() {
        queueService.openQueueSelectionGui(player)
    }

    override fun getDisplayName(): String {
        return "messages.hub.hotbar.queueCompass.title"
    }

    override fun getLore(): List<String> {
        return listOf("messages.hub.hotbar.queueCompass.description")
    }
}

