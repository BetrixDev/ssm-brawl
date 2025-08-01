package dev.betrix.superSmashMobsBrawl.passives.instances

import dev.betrix.superSmashMobsBrawl.SuperSmashMobsBrawl
import dev.betrix.superSmashMobsBrawl.extensions.setVelocity
import dev.betrix.superSmashMobsBrawl.passives.definitions.PassiveDefinition
import dev.betrix.superSmashMobsBrawl.utils.isOnGround
import gg.flyte.twilight.event.event
import gg.flyte.twilight.scheduler.TwilightRunnable
import gg.flyte.twilight.scheduler.repeatingTask
import org.bukkit.Sound
import org.bukkit.entity.Player
import org.bukkit.event.entity.EntityDamageEvent
import org.bukkit.event.entity.PlayerDeathEvent
import org.bukkit.event.player.PlayerToggleFlightEvent

class DoubleJumpPassiveInstance(definition: PassiveDefinition, player: Player) :
    PassiveInstance(definition, player) {
    private var canDoubleJump = true
    private var hasDoubleJumped = false

    private var groundCheckJob: TwilightRunnable? = null

    override fun setup() {
        player.allowFlight = true
        SuperSmashMobsBrawl.instance.logger.info("DoubleJumpPassiveInstance setup for player: ${player.name}")

        event<PlayerToggleFlightEvent> ToggleFlightEvent@{
            if (player != this@DoubleJumpPassiveInstance.player) {
                return@ToggleFlightEvent
            }

            SuperSmashMobsBrawl.instance.logger.info("PlayerToggleFlightEvent triggered for ${player.name}, canDoubleJump: $canDoubleJump")
            isCancelled = true

            if (!canDoubleJump) {
                SuperSmashMobsBrawl.instance.logger.info("Double jump not available for ${player.name}")
                return@ToggleFlightEvent
            }

            // Reset fall distance to prevent fall damage
            player.fallDistance = 0f
            player.playSound(player.location, Sound.ENTITY_BLAZE_SHOOT, 1F, 1F)
            player.setVelocity(player.location.direction, 0.9, true, 0.9, 0.0, 0.9, true)

            player.allowFlight = false
            canDoubleJump = false
            hasDoubleJumped = true

            SuperSmashMobsBrawl.instance.logger.info("Double jump executed for ${player.name}")

            groundCheckJob =
                repeatingTask(1) {
                    // More robust ground detection
                    if (isOnGround(player) || canDoubleJump) {
                        canDoubleJump = true
                        player.allowFlight = true
                        hasDoubleJumped = false
                        SuperSmashMobsBrawl.instance.logger.info("Ground detected for ${player.name}, double jump reset")
                        this.cancel()
                    }
                }

            event<PlayerDeathEvent> DeathEvent@{
                if (player != this@ToggleFlightEvent.player) {
                    return@DeathEvent
                }

                groundCheckJob?.cancel()
                canDoubleJump = true
                hasDoubleJumped = false
                SuperSmashMobsBrawl.instance.logger.info("Player death event for ${player.name}, double jump reset")
            }
        }

        // Additional fall damage protection for double jump users
        event<EntityDamageEvent> {
            if (entity == player && hasDoubleJumped) {
                when (cause) {
                    EntityDamageEvent.DamageCause.FALL -> {
                        // Cancel fall damage if player has recently double jumped
                        isCancelled = true
                        player.fallDistance = 0f
                        hasDoubleJumped = false
                        SuperSmashMobsBrawl.instance.logger.info("Fall damage cancelled for ${player.name} due to recent double jump")
                    }
                    else -> {
                        // For other damage types, just reset the double jump flag
                        hasDoubleJumped = false
                    }
                }
            }
        }
    }

    override fun teardown() {
        groundCheckJob?.cancel()
        player.allowFlight = false
        canDoubleJump = false
        hasDoubleJumped = false
        SuperSmashMobsBrawl.instance.logger.info("DoubleJumpPassiveInstance teardown for player: ${player.name}")
    }
}
