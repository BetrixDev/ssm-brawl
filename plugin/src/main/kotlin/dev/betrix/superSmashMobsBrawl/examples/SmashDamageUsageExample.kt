package dev.betrix.superSmashMobsBrawl.examples

import dev.betrix.superSmashMobsBrawl.events.SmashDamageEvent
import dev.betrix.superSmashMobsBrawl.minigames.definitions.MinigameDefinition
import dev.betrix.superSmashMobsBrawl.minigames.instances.MinigameInstance
import dev.betrix.superSmashMobsBrawl.minigames.instances.interfaces.OnSmashDamage
import dev.betrix.superSmashMobsBrawl.models.MinigameTeam
import gg.flyte.twilight.event.event
import org.bukkit.entity.Player

/**
 * Example showing how to use the SmashDamageEvent system in a mini-game.
 * This demonstrates basic damage handling and customization.
 */
class SmashDamageUsageExample(
    definition: MinigameDefinition,
    teams: List<MinigameTeam>
) : MinigameInstance(definition, teams), OnSmashDamage {
    
    // Register event listener for SmashDamageEvent
    init {
        event<SmashDamageEvent> {
            // Only handle events for players in this mini-game
            if (!isPlayerInMinigame(victim)) return@event
            
            // Call our damage handling method
            val shouldProcess = onSmashDamage(this@event)
            
            if (!shouldProcess) {
                isCancelled = true
                return@event
            }
            
            // Apply pre-damage modifications
            val modifiedDamage = onPreSmashDamage(this@event)
            
            // Call post-damage effects
            onPostSmashDamage(this@event, modifiedDamage)
        }
    }
    
    override fun onSmashDamage(event: SmashDamageEvent): Boolean {
        // Example: Cancel all fall damage in this mini-game
        if (event.damageSource == SmashDamageEvent.DamageSource.FALL) {
            event.victim.sendMessage("§aFall damage is disabled in this mini-game!")
            return false // Cancel the damage
        }
        
        // Example: Check for special conditions
        if (event.hasExtraData("ability_name")) {
            val abilityName = event.getExtraData<String>("ability_name")
            event.victim.sendMessage("§cYou were hit by ability: $abilityName")
        }
        
        return true // Allow normal damage processing
    }
    
    override fun onPreSmashDamage(event: SmashDamageEvent): Double {
        // Example: Reduce explosion damage by 50%
        if (event.damageSource == SmashDamageEvent.DamageSource.EXPLOSION) {
            return event.damage * 0.5
        }
        
        // Example: Increase melee damage by 20% for specific conditions
        if (event.damageSource == SmashDamageEvent.DamageSource.MELEE_ATTACK && 
            event.damager is Player) {
            return event.damage * 1.2
        }
        
        return event.damage // No modification
    }
    
    override fun onPostSmashDamage(event: SmashDamageEvent, finalDamage: Double) {
        // Example: Apply special effects after damage
        if (finalDamage > 10.0) {
            event.victim.sendMessage("§4§lCritical hit! You took $finalDamage damage!")
        }
        
        // Example: Track damage statistics
        if (event.damager is Player) {
            val damager = event.damager
            damager.sendMessage("§aYou dealt $finalDamage damage to ${event.victim.name}!")
        }
        
        // Example: Apply knockback effects
        if (event.knockback != null) {
            event.victim.sendMessage("§eYou were knocked back!")
        }
    }
    
    override fun onPlayerLeave(player: Player) {
        // Handle player leaving the mini-game
        player.sendMessage("§cYou left the mini-game!")
    }
    
    override fun setup(): com.github.michaelbull.result.Result<Unit, Exception> {
        // Setup the mini-game
        return com.github.michaelbull.result.Ok(Unit)
    }
    
    override fun teardown() {
        // Cleanup when the mini-game ends
    }
}