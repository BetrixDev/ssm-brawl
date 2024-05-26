package net.ssmb.passives

import com.github.shynixn.mccoroutine.bukkit.launch
import com.github.shynixn.mccoroutine.bukkit.minecraftDispatcher
import com.github.shynixn.mccoroutine.bukkit.ticks
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import net.ssmb.SSMB
import net.ssmb.dtos.minigame.MinigameStartSuccess
import net.ssmb.extensions.setVelocity
import net.ssmb.utils.isOnGround
import org.bukkit.GameMode
import org.bukkit.Sound
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.entity.PlayerDeathEvent
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

    @EventHandler(priority = EventPriority.LOWEST)
    fun onPlayerToggleFlight(event: PlayerToggleFlightEvent) {
        println("canDoubleJump: $canDoubleJump")
        if (event.player != player) return
        if (event.player.gameMode == GameMode.CREATIVE) return

        event.isCancelled = true

        if (!canDoubleJump) {
            return
        }

        player.allowFlight = false

        player.fallDistance = 0F
        player.playSound(player.location, Sound.ENTITY_BLAZE_SHOOT, 1F, 1F)
        player.setVelocity(player.location.direction, 0.9, true, 0.9, 0.0, 0.9, true)

        canDoubleJump = false

       plugin.launch {
          withContext(Dispatchers.IO) {
              while (!canDoubleJump) {
                  println(isOnGround(player))
                  if (isOnGround(player)) {
                      canDoubleJump = true
                      player.allowFlight = true
                      return@withContext this.cancel()
                  }

                  delay(1.ticks)
              }
          }
       }
    }

    @EventHandler
    fun onPlayerDeath(event: PlayerDeathEvent) {
        // This is purely to stop the coroutine from running when the player dies
        // might be better to cancel the actual job itself since that's more declarative
        if (event.player == player) {
            canDoubleJump = true
        }
    }
}
