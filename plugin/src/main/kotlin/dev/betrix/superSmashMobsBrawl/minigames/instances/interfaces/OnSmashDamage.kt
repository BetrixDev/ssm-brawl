package dev.betrix.superSmashMobsBrawl.minigames.instances.interfaces

import dev.betrix.superSmashMobsBrawl.events.SmashDamageEvent

/**
 * Interface for mini-game instances that want to handle SmashDamageEvents.
 * Implement this interface to customize damage handling in your mini-game.
 */
interface OnSmashDamage {
    
    /**
     * Called when a player takes damage in this mini-game.
     * 
     * @param event The SmashDamageEvent containing all damage information
     * @return true if the damage should be processed normally, false if it should be cancelled
     */
    fun onSmashDamage(event: SmashDamageEvent): Boolean
    
    /**
     * Called before damage is applied to a player.
     * This allows the mini-game to modify damage values or apply special effects.
     * 
     * @param event The SmashDamageEvent containing all damage information
     * @return The modified damage value, or the original damage if no modification is needed
     */
    fun onPreSmashDamage(event: SmashDamageEvent): Double {
        return event.damage
    }
    
    /**
     * Called after damage has been applied to a player.
     * This allows the mini-game to apply post-damage effects or logic.
     * 
     * @param event The SmashDamageEvent containing all damage information
     * @param finalDamage The final damage that was actually applied
     */
    fun onPostSmashDamage(event: SmashDamageEvent, finalDamage: Double) {
        // Default implementation does nothing
    }
}