package dev.betrix.superSmashMobsBrawl.abilities.instances

import dev.betrix.superSmashMobsBrawl.abilities.definitions.AbilityDefinition
import org.bukkit.entity.Player

abstract class AbilityInstance(val definition: AbilityDefinition, val player: Player) {
    private var lastUsed: Long = 0

    abstract fun setup(): Unit

    abstract fun teardown(): Unit

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
