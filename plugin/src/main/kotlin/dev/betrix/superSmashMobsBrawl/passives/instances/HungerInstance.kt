package dev.betrix.superSmashMobsBrawl.passives.instances

import dev.betrix.superSmashMobsBrawl.events.SmashDamageEvent
import dev.betrix.superSmashMobsBrawl.passives.definitions.PassiveDefinition
import gg.flyte.twilight.event.event
import gg.flyte.twilight.scheduler.TwilightRunnable
import gg.flyte.twilight.scheduler.repeatingTask
import org.bukkit.entity.Player
import org.bukkit.event.entity.PlayerDeathEvent

class HungerInstance(definition: PassiveDefinition, player: Player) :
    PassiveInstance(definition, player) {
    
    private var lastDamageTime = System.currentTimeMillis()
    private val hungerDelay = 10000L // 10 seconds without dealing damage before hunger starts
    private val hungerDamage = 1.0 // Damage per tick
    private val hungerInterval = 20L // Ticks between hunger damage (1 second)
    private val maxHungerLevel = 20 // Maximum hunger level (full bar)
    
    private var hungerTask: TwilightRunnable? = null
    
    override fun setup() {
        // Listen for when this player deals damage
        event<SmashDamageEvent> {
            if (damager != this@HungerInstance.player) return@event
            
            // Reset the last damage time
            lastDamageTime = System.currentTimeMillis()
            
            // Restore hunger if they deal damage
            if (player.foodLevel < maxHungerLevel) {
                player.foodLevel = (player.foodLevel + 2).coerceAtMost(maxHungerLevel)
            }
        }
        
        // Listen for player death to reset
        event<PlayerDeathEvent> {
            if (player != this@HungerInstance.player) return@event
            
            // Reset on death
            lastDamageTime = System.currentTimeMillis()
            player.foodLevel = maxHungerLevel
        }
        
        // Start the hunger check task
        hungerTask = repeatingTask(hungerInterval) {
            val currentTime = System.currentTimeMillis()
            val timeSinceLastDamage = currentTime - lastDamageTime
            
            // If player hasn't dealt damage in a while, start depleting hunger
            if (timeSinceLastDamage > hungerDelay) {
                // Reduce hunger bar
                if (player.foodLevel > 0) {
                    player.foodLevel = (player.foodLevel - 1).coerceAtLeast(0)
                }
                
                // If hunger is depleted, deal damage
                if (player.foodLevel <= 0 && player.health > 0) {
                    player.damage(hungerDamage)
                }
            }
        }
    }
    
    override fun teardown() {
        hungerTask?.cancel()
        // Restore full hunger when passive is removed
        player.foodLevel = maxHungerLevel
    }
}