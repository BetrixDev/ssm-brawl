package dev.betrix.superSmashMobsBrawl.passives.instances

import dev.betrix.superSmashMobsBrawl.passives.definitions.PassiveDefinition
import gg.flyte.twilight.scheduler.TwilightRunnable
import gg.flyte.twilight.scheduler.repeatingTask
import org.bukkit.entity.Player
import kotlin.math.min

class RegenerationInstance(definition: PassiveDefinition, player: Player) :
    PassiveInstance(definition, player) {
    
    // Configuration values for regeneration
    private val regenPeriodTicks = 20L  // 1 second (20 ticks)
    private val regenAmount = 0.25      // 0.25 health per period
    
    private var regenTask: TwilightRunnable? = null
    
    override fun setup() {
        // Start the regeneration task that runs every regenPeriodTicks
        regenTask = repeatingTask(regenPeriodTicks) {
            regenerateHealth()
        }
    }
    
    private fun regenerateHealth() {
        // Skip if player is dead
        if (player.isDead) {
            return
        }
        
        // Calculate new health, capped at max health
        val currentHealth = player.health
        val maxHealth = player.maxHealth
        val newHealth = min(currentHealth + regenAmount, maxHealth)
        
        // Apply the new health
        player.health = newHealth
    }
    
    override fun teardown() {
        // Cancel the regeneration task when passive is removed
        regenTask?.cancel()
    }
}