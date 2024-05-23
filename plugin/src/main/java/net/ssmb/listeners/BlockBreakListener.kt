package net.ssmb.listeners

import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.block.BlockBreakEvent

class BlockBreakListener : Listener {
    @EventHandler
    fun onBlockBreak(event: BlockBreakEvent) {
        event.isCancelled = true
    }
}