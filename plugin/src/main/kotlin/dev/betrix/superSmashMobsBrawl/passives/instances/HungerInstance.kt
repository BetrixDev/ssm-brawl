package dev.betrix.superSmashMobsBrawl.passives.instances

import dev.betrix.superSmashMobsBrawl.passives.definitions.PassiveDefinition
import gg.flyte.twilight.event.event
import gg.flyte.twilight.scheduler.TwilightRunnable
import gg.flyte.twilight.scheduler.repeatingTask
import org.bukkit.entity.Player
import org.bukkit.event.entity.FoodLevelChangeEvent
import org.bukkit.event.player.PlayerQuitEvent

class HungerInstance(
    definition: PassiveDefinition,
    player: Player
) : PassiveInstance(definition, player) {

    private var hungerTask: TwilightRunnable? = null
    private var isActive = false

    override fun setup() {
        // Set initial food level and saturation
        player.foodLevel = 20
        player.saturation = 20.0f
        
        // Start hunger maintenance task
        startHungerTask()

        // Prevent food level changes
        event<FoodLevelChangeEvent> FoodChangeEvent@{
            if (entity != this@HungerInstance.player) {
                return@FoodChangeEvent
            }

            // Cancel any food level changes
            isCancelled = true
            
            // Ensure food level stays at 20
            player.foodLevel = 20
            player.saturation = 20.0f
        }

        // Clean up on quit
        event<PlayerQuitEvent> QuitEvent@{
            if (player != this@HungerInstance.player) {
                return@QuitEvent
            }

            teardown()
        }

        isActive = true
    }

    private fun startHungerTask() {
        hungerTask = repeatingTask(40) { // Run every 2 seconds (40 ticks)
            if (!isActive || !player.isOnline) {
                return@repeatingTask
            }

            // Maintain full food level and saturation
            if (player.foodLevel < 20) {
                player.foodLevel = 20
            }
            
            if (player.saturation < 20.0f) {
                player.saturation = 20.0f
            }
        }
    }

    override fun teardown() {
        isActive = false
        hungerTask?.cancel()
        hungerTask = null
    }
}