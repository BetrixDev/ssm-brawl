package dev.betrix.supersmashmobsbrawl.passives

import com.github.shynixn.mccoroutine.bukkit.launch
import com.github.shynixn.mccoroutine.bukkit.ticks
import dev.betrix.supersmashmobsbrawl.SuperSmashMobsBrawl
import dev.betrix.supersmashmobsbrawl.extensions.setVelocity
import dev.betrix.supersmashmobsbrawl.utils.isOnGround
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import org.bukkit.GameMode
import org.bukkit.Sound
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.HandlerList
import org.bukkit.event.player.PlayerToggleFlightEvent

class DoubleJumpPassive(private val player: Player) : SSMBPassive() {
    private val plugin = SuperSmashMobsBrawl.instance

    init {
        plugin.server.pluginManager.registerEvents(this, plugin)
        player.allowFlight = true
    }

    override fun destroyPassive() {
        HandlerList.unregisterAll(this)
    }

    private var canDoubleJump = true

    @EventHandler
    fun onPlayerToggleFlight(event: PlayerToggleFlightEvent) {
        if (event.player != player || !canDoubleJump || event.player.gameMode == GameMode.CREATIVE) return

        event.isCancelled = true

        player.isFlying = true
        player.allowFlight = false
        player.fallDistance = 0F
        player.playSound(player.location, Sound.ENTITY_BLAZE_SHOOT, 1F, 1F)
        player.setVelocity(player.location.direction, 0.9, true, 0.9, 0.0, 0.9, true)

        canDoubleJump = false

        plugin.launch {
            while (true) {
                if (isOnGround(player)) {
                    canDoubleJump = true
                    player.allowFlight = true
                    this.cancel()
                }

                delay(1.ticks)
            }
        }
    }
}