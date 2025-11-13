package dev.betrix.superSmashMobsBrawl.passives

import dev.betrix.superSmashMobsBrawl.extensions.sendDebugMessage
import gg.flyte.twilight.event.event
import gg.flyte.twilight.scheduler.repeatingTask
import org.bukkit.GameMode
import org.bukkit.entity.Player
import org.bukkit.event.player.PlayerToggleFlightEvent

open class EnergyDoubleJumpPassive(
    id: String = "energy_double_jump",
    player: Player,
    protected open val expUsed: Float = 0.1f,
) : DoubleJumpPassive(id, player) {

    override val rechargeDelayMs: Long = 100L
    protected open val needsExactXP: Boolean = false

    override fun setupRechargeTask() {
        runnables.add(
            repeatingTask(1) {
                if (player.gameMode == GameMode.CREATIVE) {
                    return@repeatingTask
                }

                if (groundCheck()) {
                    canDoubleJump = true
                    player.allowFlight = true
                } else if (
                    System.currentTimeMillis() - lastJumpTimeMs >= rechargeDelayMs && canUseJump()
                ) {
                    canDoubleJump = true
                    player.allowFlight = true
                }
            }
        )
    }

    override fun setupFlightToggleListener() {
        listeners.add(
            event<PlayerToggleFlightEvent> ToggleFlightEvent@{
                if (
                    player != this@EnergyDoubleJumpPassive.player ||
                        player.gameMode == GameMode.CREATIVE ||
                        player.gameMode == GameMode.SPECTATOR
                ) {
                    return@ToggleFlightEvent
                }

                isCancelled = true

                // For energy-based jumps, check if they have enough energy
                if (!canUseJump()) {
                    player.sendDebugMessage("[EDJ] Not enough energy to jump")
                    player.allowFlight = false
                    return@ToggleFlightEvent
                }

                player.sendDebugMessage("[EDJ] Energy double jump activated")

                player.isFlying = false
                player.allowFlight = false
                player.fallDistance = 0f

                playDoubleJumpSound()
                activate()

                lastJumpTimeMs = System.currentTimeMillis()
            }
        )
    }

    override fun canUseJump(): Boolean {
        return player.exp > 0f
    }
}
