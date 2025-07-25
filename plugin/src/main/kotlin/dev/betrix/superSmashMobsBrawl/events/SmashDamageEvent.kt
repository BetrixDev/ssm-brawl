package dev.betrix.superSmashMobsBrawl.events

import gg.flyte.twilight.event.TwilightEvent
import org.bukkit.entity.Entity
import org.bukkit.entity.Player
import org.bukkit.event.Cancellable
import org.bukkit.event.HandlerList
import org.bukkit.util.Vector
import java.time.Instant

class SmashDamageEvent(
    val victim: Player,
    val damager: Entity?,
    val damage: Double,
    val knockback: Vector?,
    val damageSource: DamageSource,
    val extraData: Map<String, Any> = emptyMap(),
    async: Boolean = false
) : TwilightEvent(async), Cancellable {
    
    private var cancelled = false
    
    enum class DamageSource {
        MELEE_ATTACK,
        ABILITY,
        PASSIVE,
        PROJECTILE,
        EXPLOSION,
        FALL,
        ENVIRONMENT,
        OTHER
    }
    
    @Suppress("UNCHECKED_CAST")
    inline fun <reified T> getExtraData(key: String): T? {
        return extraData[key] as? T
    }
    
    inline fun <reified T> getExtraData(key: String, default: T): T {
        return getExtraData<T>(key) ?: default
    }
    
    fun hasExtraData(key: String): Boolean {
        return extraData.containsKey(key)
    }
    
    override fun isCancelled(): Boolean = cancelled
    
    override fun setCancelled(cancel: Boolean) {
        cancelled = cancel
    }
    
    companion object {
        private val HANDLERS = HandlerList()
        
        @JvmStatic
        fun getHandlerList(): HandlerList = HANDLERS
    }
    
    override fun getHandlers(): HandlerList = HANDLERS
}