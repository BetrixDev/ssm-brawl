package dev.betrix.superSmashMobsBrawl.abilities.instances

import dev.betrix.superSmashMobsBrawl.abilities.definitions.AbilityDefinition
import gg.flyte.twilight.event.TwilightListener
import org.bukkit.entity.Player

abstract class AbilityInstance(val definition: AbilityDefinition, val player: Player) {
    private var lastUsed: Long = 0
    protected val listeners = arrayListOf<TwilightListener>()

    abstract fun setup()

    open fun teardown() {
        listeners.forEach { it.unregister() }
    }

    abstract fun activate(): Boolean

    open fun canActivate(): Boolean {
        return !isOnCooldown()
    }

    fun isOnCooldown(): Boolean {
        val cooldownMs = definition.metadata.cooldown * 1000L
        return System.currentTimeMillis() - lastUsed < cooldownMs
    }

    fun getRemainingCooldown(): Int {
        val cooldownMs = definition.metadata.cooldown * 1000L
        val elapsed = System.currentTimeMillis() - lastUsed
        return ((cooldownMs - elapsed) / 1000).coerceAtLeast(0).toInt()
    }

    protected fun setCooldown() {
        lastUsed = System.currentTimeMillis()
    }
}
