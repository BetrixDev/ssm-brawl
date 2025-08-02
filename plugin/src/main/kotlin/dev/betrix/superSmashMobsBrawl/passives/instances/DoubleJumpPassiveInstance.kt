package dev.betrix.superSmashMobsBrawl.passives.instances

import dev.betrix.superSmashMobsBrawl.SuperSmashMobsBrawl
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
        SuperSmashMobsBrawl.instance.logger.info("Setting up double jump for ${player.name}")
        player.allowFlight = true
        SuperSmashMobsBrawl.instance.logger.info("Set allowFlight=true for ${player.name}, allowFlight=${player.allowFlight}")

        // Add a periodic check to monitor flight status
        val flightCheckTask = repeatingTask(20) { // Every second
            if (!player.allowFlight && canDoubleJump && isOnGround(player)) {
                SuperSmashMobsBrawl.instance.logger.warning("Flight was disabled for ${player.name}, re-enabling...")
                player.allowFlight = true
            }
        }
        runnables.add(flightCheckTask)

        val flightListener = event<PlayerToggleFlightEvent> ToggleFlightEvent@{
            SuperSmashMobsBrawl.instance.logger.info("PlayerToggleFlightEvent triggered for ${this.player.name}")
            
            if (player != this@DoubleJumpPassiveInstance.player) {
                return@ToggleFlightEvent
            }

            isCancelled = true

            if (!canDoubleJump) {
                SuperSmashMobsBrawl.instance.logger.info("Cannot double jump - cooldown active for ${player.name}")
                return@ToggleFlightEvent
            }

            SuperSmashMobsBrawl.instance.logger.info("Performing double jump for ${player.name}")
            player.fallDistance = 0f
            player.playSound(player.location, Sound.ENTITY_BLAZE_SHOOT, 1F, 1F)
            player.setVelocity(player.location.direction, 0.9, true, 0.9, 0.0, 0.9, true)

            player.allowFlight = false
            canDoubleJump = false

            val groundCheckJob = repeatingTask(1) {
                if (isOnGround(player) || canDoubleJump) {
                    canDoubleJump = true
                    player.allowFlight = true
                    this.cancel()
                    runnables.remove(this)
                }
            }
            runnables.add(groundCheckJob)

            val deathListener = event<PlayerDeathEvent> DeathEvent@{
                if (player != this@ToggleFlightEvent.player) {
                    return@DeathEvent
                }

                runnables.forEach { it.cancel() }
                runnables.clear()
                canDoubleJump = true
            }
            listeners.add(deathListener)
        }
        listeners.add(flightListener)
        
        SuperSmashMobsBrawl.instance.logger.info("Double jump event listener registered for ${player.name}")
    }

    override fun teardown() {
        SuperSmashMobsBrawl.instance.logger.info("Tearing down double jump for ${player.name}")
        super.teardown()
        player.allowFlight = false
        canDoubleJump = false
    }
}
