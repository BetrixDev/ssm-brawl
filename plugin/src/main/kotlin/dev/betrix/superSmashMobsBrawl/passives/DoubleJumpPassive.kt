package dev.betrix.superSmashMobsBrawl.passives

import dev.betrix.superSmashMobsBrawl.extensions.isOnBlock
import dev.betrix.superSmashMobsBrawl.extensions.sendDebugMessage
import dev.betrix.superSmashMobsBrawl.extensions.setVelocity
import dev.betrix.superSmashMobsBrawl.utils.isOnGround
import gg.flyte.twilight.event.event
import gg.flyte.twilight.scheduler.repeatingTask
import org.bukkit.GameMode
import org.bukkit.Sound
import org.bukkit.entity.Player
import org.bukkit.event.entity.EntityDamageEvent
import org.bukkit.event.entity.PlayerDeathEvent
import org.bukkit.event.player.PlayerToggleFlightEvent

open class DoubleJumpPassive(id: String = "double_jump", player: Player) :
    BrawlPassive(id, player) {

    protected open val power: Double = 0.9
    protected open val height: Double = 0.9
    protected open val doubleJumpSound: Sound = Sound.ENTITY_BLAZE_SHOOT
    protected open val rechargeDelayMs: Long = 0L

    protected var lastJumpTimeMs: Long = 0L
    protected var canDoubleJump: Boolean = true

    override fun setup() {
        player.allowFlight = true
        setupRechargeTask()
        setupFlightToggleListener()
        setupDeathListener()
        setupFallDamageListener()
        super.setup()
    }

    protected open fun setupRechargeTask() {
        runnables.add(
            repeatingTask(20) {
                if (!player.allowFlight && canDoubleJump && isOnGround(player)) {
                    player.allowFlight = true
                }
            }
        )

        runnables.add(
            repeatingTask(1) {
                if (System.currentTimeMillis() - lastJumpTimeMs < rechargeDelayMs) {
                    return@repeatingTask
                }

                if ((groundCheck() || canDoubleJump) && (!canDoubleJump || !player.allowFlight)) {
                    canDoubleJump = true
                    player.allowFlight = true
                }
            }
        )
    }

    protected open fun setupFlightToggleListener() {
        listeners.add(
            event<PlayerToggleFlightEvent> ToggleFlightEvent@{
                if (
                    player != this@DoubleJumpPassive.player ||
                        player.gameMode == GameMode.CREATIVE ||
                        player.gameMode == GameMode.SPECTATOR
                ) {
                    return@ToggleFlightEvent
                }

                isCancelled = true

                if (!canDoubleJump) {
                    player.sendDebugMessage("[DJ] You cannot double jump right now")
                    return@ToggleFlightEvent
                }

                player.sendDebugMessage("[DJ] Double jump activated")

                player.isFlying = false
                player.allowFlight = false
                player.fallDistance = 0f

                playDoubleJumpSound()
                activate()

                canDoubleJump = false
                lastJumpTimeMs = System.currentTimeMillis()
            }
        )
    }

    protected open fun setupDeathListener() {
        listeners.add(
            event<PlayerDeathEvent> DeathEvent@{
                if (this@DoubleJumpPassive.player != player) {
                    return@DeathEvent
                }

                player.sendDebugMessage("[DJ] Resetting double jump status due to death")
                canDoubleJump = true
            }
        )
    }

    protected open fun setupFallDamageListener() {
        listeners.add(
            event<EntityDamageEvent> DamageEvent@{
                if (entity != this@DoubleJumpPassive.player) {
                    return@DamageEvent
                }

                if (cause == EntityDamageEvent.DamageCause.FALL) {
                    isCancelled = true
                    player.sendDebugMessage("[DJ] Fall damage cancelled")
                }
            }
        )
    }

    protected open fun activate() {
        player.setVelocity(player.location.direction, power, true, power, 0.0, height, true)
    }

    protected open fun groundCheck(): Boolean {
        return player.isOnBlock()
    }

    protected open fun canUseJump(): Boolean {
        return canDoubleJump
    }

    protected open fun playDoubleJumpSound() {
        player.world.playSound(player.location, doubleJumpSound, 1f, 1f)
    }

    override fun teardown() {
        super.teardown()
        if (player.gameMode != GameMode.SPECTATOR && player.gameMode != GameMode.CREATIVE) {
            player.isFlying = false
            player.allowFlight = false
        }
        canDoubleJump = false
    }
}
