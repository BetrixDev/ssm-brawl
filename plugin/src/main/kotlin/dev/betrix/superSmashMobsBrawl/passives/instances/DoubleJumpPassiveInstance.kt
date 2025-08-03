package dev.betrix.superSmashMobsBrawl.passives.instances

import dev.betrix.superSmashMobsBrawl.extensions.isOnBlock
import dev.betrix.superSmashMobsBrawl.extensions.sendDebugMessage
import dev.betrix.superSmashMobsBrawl.extensions.setVelocity
import dev.betrix.superSmashMobsBrawl.passives.definitions.PassiveDefinition
import dev.betrix.superSmashMobsBrawl.utils.isOnGround
import gg.flyte.twilight.event.event
import gg.flyte.twilight.scheduler.repeatingTask
import org.bukkit.Sound
import org.bukkit.entity.Player
import org.bukkit.event.entity.PlayerDeathEvent
import org.bukkit.event.player.PlayerToggleFlightEvent

class DoubleJumpPassiveInstance(definition: PassiveDefinition, player: Player) :
    PassiveInstance(definition, player) {
    private var canDoubleJump = true

    override fun setup() {
        player.allowFlight = true

        // Add a periodic check to monitor flight status
        runnables.add(
            repeatingTask(20) {
                if (!player.allowFlight && canDoubleJump && isOnGround(player)) {
                    player.allowFlight = true
                }
            }
        )

        listeners.add(
            event<PlayerToggleFlightEvent> ToggleFlightEvent@{
                if (player != this@DoubleJumpPassiveInstance.player) {
                    return@ToggleFlightEvent
                }

                isCancelled = true
                player.isFlying = false
                player.allowFlight = false
                player.fallDistance = 0f

                if (!canDoubleJump) {
                    player.sendDebugMessage("[DJ] You cannot double jump right now")
                    return@ToggleFlightEvent
                }

                player.sendDebugMessage("[DJ] Double jump activated")

                player.playSound(player.location, Sound.ENTITY_BLAZE_SHOOT, 1F, 1F)
                player.setVelocity(player.location.direction, 0.9, true, 0.9, 0.0, 0.9, true)

                canDoubleJump = false

                runnables.add(
                    repeatingTask(1) {
                        if (player.isOnBlock() || canDoubleJump) {
                            player.sendDebugMessage("[DJ] You have hit the ground")
                            canDoubleJump = true
                            player.allowFlight = true

                            this.cancel()
                        }
                    }
                )
            }
        )

        listeners.add(
            event<PlayerDeathEvent> DeathEvent@{
                if (this@DoubleJumpPassiveInstance.player != player) {
                    return@DeathEvent
                }

                player.sendDebugMessage("[DJ] Resetting double jump status due to death")
                canDoubleJump = true
            }
        )

        super.setup()
    }

    override fun teardown() {
        super.teardown()
        player.allowFlight = false
        canDoubleJump = false
    }
}
