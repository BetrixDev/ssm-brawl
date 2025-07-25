package dev.betrix.superSmashMobsBrawl.services

import dev.betrix.superSmashMobsBrawl.events.SmashDamageEvent
import dev.betrix.superSmashMobsBrawl.minigames.instances.MinigameInstance
import org.bukkit.entity.Entity
import org.bukkit.entity.Player
import org.bukkit.plugin.Plugin
import org.bukkit.util.Vector

/**
 * Service for handling damage events in Super Smash Mobs Brawl mini-games.
 * Provides a convenient API for firing SmashDamageEvents and managing damage handling.
 */
object SmashDamageService {
    private lateinit var plugin: Plugin
    
    /**
     * Initialize the service with the plugin instance
     */
    fun initialize(pluginInstance: Plugin) {
        plugin = pluginInstance
    }
    
    /**
     * Fire a SmashDamageEvent for a player taking damage
     */
    fun fireDamageEvent(
        victim: Player,
        damager: Entity? = null,
        damage: Double,
        knockback: Vector? = null,
        damageSource: SmashDamageEvent.DamageSource = SmashDamageEvent.DamageSource.OTHER,
        extraData: Map<String, Any> = emptyMap(),
        async: Boolean = false
    ): SmashDamageEvent {
        val event = SmashDamageEvent(
            victim = victim,
            damager = damager,
            damage = damage,
            knockback = knockback,
            damageSource = damageSource,
            extraData = extraData,
            async = async
        )
        
        plugin.server.pluginManager.callEvent(event)
        return event
    }
    
    /**
     * Fire a melee attack damage event
     */
    fun fireMeleeDamageEvent(
        victim: Player,
        damager: Player,
        damage: Double,
        knockback: Vector? = null,
        extraData: Map<String, Any> = emptyMap()
    ): SmashDamageEvent {
        return fireDamageEvent(
            victim = victim,
            damager = damager,
            damage = damage,
            knockback = knockback,
            damageSource = SmashDamageEvent.DamageSource.MELEE_ATTACK,
            extraData = extraData
        )
    }
    
    /**
     * Fire an ability damage event
     */
    fun fireAbilityDamageEvent(
        victim: Player,
        damager: Player,
        damage: Double,
        knockback: Vector? = null,
        abilityName: String,
        extraData: Map<String, Any> = emptyMap()
    ): SmashDamageEvent {
        val data = extraData.toMutableMap()
        data["ability_name"] = abilityName
        
        return fireDamageEvent(
            victim = victim,
            damager = damager,
            damage = damage,
            knockback = knockback,
            damageSource = SmashDamageEvent.DamageSource.ABILITY,
            extraData = data
        )
    }
    
    /**
     * Fire a passive damage event
     */
    fun firePassiveDamageEvent(
        victim: Player,
        damager: Player,
        damage: Double,
        knockback: Vector? = null,
        passiveName: String,
        extraData: Map<String, Any> = emptyMap()
    ): SmashDamageEvent {
        val data = extraData.toMutableMap()
        data["passive_name"] = passiveName
        
        return fireDamageEvent(
            victim = victim,
            damager = damager,
            damage = damage,
            knockback = knockback,
            damageSource = SmashDamageEvent.DamageSource.PASSIVE,
            extraData = data
        )
    }
    
    /**
     * Fire a projectile damage event
     */
    fun fireProjectileDamageEvent(
        victim: Player,
        damager: Player,
        damage: Double,
        knockback: Vector? = null,
        projectileType: String,
        extraData: Map<String, Any> = emptyMap()
    ): SmashDamageEvent {
        val data = extraData.toMutableMap()
        data["projectile_type"] = projectileType
        
        return fireDamageEvent(
            victim = victim,
            damager = damager,
            damage = damage,
            knockback = knockback,
            damageSource = SmashDamageEvent.DamageSource.PROJECTILE,
            extraData = data
        )
    }
    
    /**
     * Fire an explosion damage event
     */
    fun fireExplosionDamageEvent(
        victim: Player,
        damager: Player? = null,
        damage: Double,
        knockback: Vector? = null,
        explosionRadius: Double,
        extraData: Map<String, Any> = emptyMap()
    ): SmashDamageEvent {
        val data = extraData.toMutableMap()
        data["explosion_radius"] = explosionRadius
        
        return fireDamageEvent(
            victim = victim,
            damager = damager,
            damage = damage,
            knockback = knockback,
            damageSource = SmashDamageEvent.DamageSource.EXPLOSION,
            extraData = data
        )
    }
    
    /**
     * Fire a fall damage event
     */
    fun fireFallDamageEvent(
        victim: Player,
        damage: Double,
        fallDistance: Float,
        extraData: Map<String, Any> = emptyMap()
    ): SmashDamageEvent {
        val data = extraData.toMutableMap()
        data["fall_distance"] = fallDistance
        
        return fireDamageEvent(
            victim = victim,
            damage = damage,
            damageSource = SmashDamageEvent.DamageSource.FALL,
            extraData = data
        )
    }
    
    /**
     * Check if a player is currently in a mini-game
     */
    fun isPlayerInMinigame(player: Player): Boolean {
        // This would need to be implemented based on how mini-game instances are tracked
        // For now, we'll return false as a placeholder
        return false
    }
    
    /**
     * Get the current mini-game instance for a player
     */
    fun getPlayerMinigameInstance(player: Player): MinigameInstance? {
        // This would need to be implemented based on how mini-game instances are tracked
        // For now, we'll return null as a placeholder
        return null
    }
}