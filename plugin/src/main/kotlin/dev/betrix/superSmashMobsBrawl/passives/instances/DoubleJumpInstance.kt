package dev.betrix.superSmashMobsBrawl.passives.instances

import dev.betrix.superSmashMobsBrawl.extensions.setVelocity
import dev.betrix.superSmashMobsBrawl.passives.definitions.PassiveDefinition
import dev.betrix.superSmashMobsBrawl.utils.isOnGround
import gg.flyte.twilight.event.event
import gg.flyte.twilight.scheduler.TwilightRunnable
import gg.flyte.twilight.scheduler.repeatingTask
import org.bukkit.Sound
import org.bukkit.entity.Player
import org.bukkit.event.entity.PlayerDeathEvent
import org.bukkit.event.player.PlayerToggleFlightEvent

class DoubleJumpInstance(definition: PassiveDefinition, player: Player) :
    PassiveInstance(definition, player) {
    private var canDoubleJump = true

    private var groundCheckJob: TwilightRunnable? = null

    override fun setup() {
        player.allowFlight = true

        event<PlayerToggleFlightEvent> ToggleFlightEvent@{
            if (player != this@DoubleJumpInstance.player) {
                return@ToggleFlightEvent
            }

            isCancelled = true

            if (!canDoubleJump) {
                return@ToggleFlightEvent
            }

            player.fallDistance = 0f
            player.playSound(player.location, Sound.ENTITY_BLAZE_SHOOT, 1F, 1F)
            player.setVelocity(player.location.direction, 0.9, true, 0.9, 0.0, 0.9, true)

            player.allowFlight = false
            canDoubleJump = false

            groundCheckJob =
                repeatingTask(1) {
                    if (isOnGround(player) || canDoubleJump) {
                        canDoubleJump = true
                        player.allowFlight = true
                        this.cancel()
                    }
                }

            event<PlayerDeathEvent> DeathEvent@{
                if (player != this@ToggleFlightEvent.player) {
                    return@DeathEvent
                }

                groundCheckJob?.cancel()
                canDoubleJump = true
            }
        }
    }

    override fun teardown() {
        groundCheckJob?.cancel()
        player.allowFlight = false
        canDoubleJump = false
    }
}
