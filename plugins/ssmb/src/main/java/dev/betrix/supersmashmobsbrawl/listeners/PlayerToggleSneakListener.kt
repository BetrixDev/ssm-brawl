package dev.betrix.supersmashmobsbrawl.listeners

import dev.betrix.supersmashmobsbrawl.enums.TaggedKeyBool
import dev.betrix.supersmashmobsbrawl.extensions.getMetadata
import dev.betrix.supersmashmobsbrawl.extensions.setMetadata
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerToggleSneakEvent

class PlayerToggleSneakListener : Listener {

    @EventHandler
    fun onPlayerToggleSneak(event: PlayerToggleSneakEvent) {
        val player = event.player

        if (player.getMetadata(TaggedKeyBool.PLAYER_IS_EXPLODE_ACTIVE) == true) {
            player.setMetadata {
                set(TaggedKeyBool.PLAYER_IS_EXPLODE_ACTIVE, false)
                player.exp = 0F
                player.level = 0
                player.walkSpeed = 0.2F
            }
        }
    }
}