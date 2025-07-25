package dev.betrix.superSmashMobsBrawl.services

import dev.betrix.superSmashMobsBrawl.events.SmashDamageEvent
import dev.betrix.superSmashMobsBrawl.minigames.instances.MinigameInstance
import org.bukkit.entity.Entity
import org.bukkit.entity.Player
import org.bukkit.plugin.Plugin
import org.bukkit.util.Vector

object SmashDamageService {
    private lateinit var plugin: Plugin
    
    fun initialize(pluginInstance: Plugin) {
        plugin = pluginInstance
    }
    
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
    
    fun isPlayerInMinigame(player: Player): Boolean {
        return false
    }
    
    fun getPlayerMinigameInstance(player: Player): MinigameInstance? {
        return null
    }
}