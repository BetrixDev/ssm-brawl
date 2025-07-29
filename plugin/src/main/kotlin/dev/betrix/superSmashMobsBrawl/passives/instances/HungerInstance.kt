package dev.betrix.superSmashMobsBrawl.passives.instances

import dev.betrix.superSmashMobsBrawl.events.Damager
import dev.betrix.superSmashMobsBrawl.events.SmashDamageEvent
import dev.betrix.superSmashMobsBrawl.passives.definitions.PassiveDefinition
import dev.betrix.superSmashMobsBrawl.utils.mm
import gg.flyte.twilight.event.event
import gg.flyte.twilight.scheduler.TwilightRunnable
import gg.flyte.twilight.scheduler.repeatingTask
import org.bukkit.GameMode
import org.bukkit.entity.Player
import org.bukkit.event.entity.PlayerDeathEvent
import kotlin.math.max
import kotlin.math.min

class HungerInstance(definition: PassiveDefinition, player: Player) :
    PassiveInstance(definition, player) {
    
    private val secondsToDrain = 10.0
    private val hungerRestoreDelayMs = 250L
    private var hungerTicks = 0L
    private var lastHungerRestoreMs = System.currentTimeMillis()
    
    private var hungerTask: TwilightRunnable? = null
    
    override fun setup() {
        // Listen for when this player deals damage to restore hunger
        event<SmashDamageEvent> {
            // Check if this player is the damager
            val isThisPlayerDamager = when (damager) {
                is Damager.LivingEntity -> damager.livingEntity == this@HungerInstance.player
                is Damager.System -> false
                null -> false
            }
            
            if (!isThisPlayerDamager) return@event
            
            // Restore hunger based on damage dealt
            hungerRestore(damage)
        }
        
        // Listen for player death to reset
        event<PlayerDeathEvent> {
            if (player != this@HungerInstance.player) return@event
            
            // Reset hunger on death
            player.foodLevel = 20
            lastHungerRestoreMs = System.currentTimeMillis()
        }
        
        // Start the hunger task that runs every tick (20 times per second)
        hungerTask = repeatingTask(1L) {
            activate()
        }
    }
    
    private fun activate() {
        // Skip if player is in creative mode
        if (player.gameMode == GameMode.CREATIVE) {
            return
        }
        
        // Increment hunger ticks and wrap around at 10
        hungerTicks = (hungerTicks + 1) % 10
        
        // Set saturation and exhaustion to prevent natural hunger depletion
        player.saturation = 3f
        player.exhaustion = 0f
        
        // If food level is 0, deal damage
        if (player.foodLevel <= 0) {
            player.sendMessage(mm("<red>Attack other players to restore hunger!</red>"))
            
            val damageEvent = SmashDamageEvent(
                victim = player,
                damager = Damager.System,
                damage = 1.0,
                knockbackMultiplier = 0.0
            )
            // Note: In the Java version they set damage cause, damager name, and reason
            // but our SmashDamageEvent doesn't have those fields currently
            damageEvent.callEvent()
            return
        }
        
        // Every 10 ticks (0.5 seconds), reduce food level by 1
        if (hungerTicks == 0L) {
            player.foodLevel = max(0, player.foodLevel - 1)
        }
    }
    
    private fun hungerRestore(damage: Double) {
        // Check if enough time has passed since last restore
        if ((System.currentTimeMillis() - lastHungerRestoreMs) < hungerRestoreDelayMs) {
            return
        }
        
        lastHungerRestoreMs = System.currentTimeMillis()
        
        // Calculate amount to restore (half of damage dealt, minimum 1)
        val amount = max(1, (damage / 2).toInt())
        
        // Restore food level, capped at 20
        player.foodLevel = min(20, player.foodLevel + amount)
    }
    
    override fun teardown() {
        hungerTask?.cancel()
        // Restore full hunger when passive is removed
        player.foodLevel = 20
    }
}