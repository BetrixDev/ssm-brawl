package dev.betrix.superSmashMobsBrawl.services

import dev.betrix.superSmashMobsBrawl.Manageable
import dev.betrix.superSmashMobsBrawl.events.BrawlDamageEvent
import dev.betrix.superSmashMobsBrawl.events.BrawlDeathEvent
import dev.betrix.superSmashMobsBrawl.events.Damager
import gg.flyte.twilight.event.event
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap
import org.bukkit.entity.Player
import org.koin.core.component.KoinComponent

data class ComboKey(val attackerId: UUID, val victimId: UUID)

data class ComboData(
    var count: Int = 0,
    var lastHitTime: Long = System.currentTimeMillis()
)

/**
 * Service for tracking combo counts between players.
 * 
 * A combo is a series of consecutive hits from one player to another.
 * Combos decay after a configurable timeout period without a hit.
 * 
 * Configuration can be adjusted via the comboDecayTimeMs property.
 */
object ComboTrackerService : KoinComponent, Manageable() {
    private val combos = ConcurrentHashMap<ComboKey, ComboData>()
    
    /**
     * Time in milliseconds before a combo resets due to inactivity.
     * Default: 4000ms (4 seconds)
     */
    var comboDecayTimeMs: Long = 4000L
    
    override fun setup() {
        // Listen to damage events to track combos
        listeners.add(
            event<BrawlDamageEvent> {
                val attackerPlayer = when (damager) {
                    is Damager.DamagerLivingEntity -> (damager.livingEntity as? Player)
                    else -> null
                } ?: return@event
                
                val victimPlayer = victim as? Player ?: return@event
                
                if (attackerPlayer.uniqueId == victimPlayer.uniqueId) {
                    return@event
                }
                
                incrementCombo(attackerPlayer, victimPlayer)
            }
        )
        
        // Reset combos on death
        listeners.add(
            event<BrawlDeathEvent> {
                resetCombosForPlayer(player)
            }
        )
        
        super.setup()
    }
    
    /**
     * Gets the current combo count for an attacker against a victim.
     * Returns 0 if the combo has decayed or doesn't exist.
     */
    fun getCombo(attacker: Player, victim: Player): Int {
        val key = ComboKey(attacker.uniqueId, victim.uniqueId)
        val combo = combos[key] ?: return 0
        
        val timeSinceLastHit = System.currentTimeMillis() - combo.lastHitTime
        if (timeSinceLastHit > comboDecayTimeMs) {
            combos.remove(key)
            return 0
        }
        
        return combo.count
    }
    
    /**
     * Increments the combo count for an attacker against a victim.
     * Resets the combo if it has decayed.
     */
    fun incrementCombo(attacker: Player, victim: Player) {
        val key = ComboKey(attacker.uniqueId, victim.uniqueId)
        val currentTime = System.currentTimeMillis()
        
        val combo = combos.getOrPut(key) { ComboData() }
        
        val timeSinceLastHit = currentTime - combo.lastHitTime
        if (timeSinceLastHit > comboDecayTimeMs) {
            combo.count = 1
        } else {
            combo.count++
        }
        
        combo.lastHitTime = currentTime
    }
    
    /**
     * Resets the combo count for a specific attacker-victim pair.
     */
    fun resetCombo(attacker: Player, victim: Player) {
        val key = ComboKey(attacker.uniqueId, victim.uniqueId)
        combos.remove(key)
    }
    
    /**
     * Resets all combos involving a specific player (as attacker or victim).
     */
    fun resetCombosForPlayer(player: Player) {
        combos.keys.removeIf { it.attackerId == player.uniqueId || it.victimId == player.uniqueId }
    }
    
    /**
     * Resets all tracked combos.
     */
    fun resetAllCombos() {
        combos.clear()
    }
}

