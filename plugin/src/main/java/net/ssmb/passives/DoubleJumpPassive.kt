package net.ssmb.passives

import com.github.shynixn.mccoroutine.bukkit.launch
import com.github.shynixn.mccoroutine.bukkit.ticks
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import net.ssmb.SSMB
import net.ssmb.extensions.setVelocity
import net.ssmb.utils.isOnGround
import org.bukkit.GameMode
import org.bukkit.Sound
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.HandlerList
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerToggleFlightEvent

class DoubleJumpPassive(private val player: Player) : IPassive, Listener {
    private val plugin = SSMB.instance

    private var canDoubleJump = true

    override fun createPassive() {
        plugin.server.pluginManager.registerEvents(this, plugin)
        player.allowFlight = true
        canDoubleJump = true
    }

    override fun destroyPassive() {
        HandlerList.unregisterAll(this)
        player.allowFlight = false
        canDoubleJump = false
    }

    @EventHandler
    fun onPlayerToggleFlight(event: PlayerToggleFlightEvent) {
        if (!canDoubleJump) return
        if (event.player != player) return
        if (event.player.gameMode == GameMode.CREATIVE) return

        event.isCancelled = true

        player.isFlying = true
        player.allowFlight = false
        player.fallDistance = 0F
        player.playSound(player.location, Sound.ENTITY_BLAZE_SHOOT, 1F, 1F)
        player.setVelocity(player.location.direction, 0.9, true, 0.9, 0.0, 0.9, true)

        canDoubleJump = true

        plugin.launch {
            var times = 0

            while (true) {
                // Keep track of amount of iterations in case isOnGround() never returns true,
                // so we aren't always looping
                times++

                if (isOnGround(player) || times > 20 * 60) {
                    canDoubleJump = true
                    player.allowFlight = true
                    this.cancel()
                }

                delay(1.ticks)
            }
        }
    }
}
