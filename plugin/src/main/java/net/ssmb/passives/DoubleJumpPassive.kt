package net.ssmb.passives

import com.github.shynixn.mccoroutine.bukkit.launch
import com.github.shynixn.mccoroutine.bukkit.ticks
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import net.ssmb.SSMB
import net.ssmb.dtos.minigame.MinigameStartSuccess
import net.ssmb.extensions.setVelocity
import net.ssmb.utils.isOnGround
import org.bukkit.GameMode
import org.bukkit.Sound
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.player.PlayerToggleFlightEvent

class DoubleJumpPassive(
    private val player: Player,
    private val passiveData: MinigameStartSuccess.PlayerData.KitData.PassiveEntry.PassiveData
) : SsmbPassive(player, passiveData) {
    private val plugin = SSMB.instance

    private var canDoubleJump = true

    override fun initializePassive() {
        super.initializePassive()
        player.allowFlight = true
        canDoubleJump = true
    }

    override fun destroyPassive() {
        super.destroyPassive()
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
