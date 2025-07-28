package dev.betrix.superSmashMobsBrawl.abilities.instances

import dev.betrix.superSmashMobsBrawl.SuperSmashMobsBrawl
import dev.betrix.superSmashMobsBrawl.abilities.definitions.AbilityDefinition
import dev.betrix.superSmashMobsBrawl.di.Injectable
import dev.betrix.superSmashMobsBrawl.lifecycle.Manageable
import dev.betrix.superSmashMobsBrawl.utils.mm
import gg.flyte.twilight.Twilight
import gg.flyte.twilight.event.TwilightListener
import gg.flyte.twilight.scheduler.TwilightRunnable
import kotlinx.coroutines.Job
import org.bukkit.entity.Player
import org.koin.core.component.inject

abstract class AbilityInstance(val definition: AbilityDefinition, val player: Player) : Manageable, Injectable {
    private var lastUsed: Long = 0
    protected val listeners = arrayListOf<TwilightListener>()
    protected val runnables = arrayListOf<TwilightRunnable>()
    protected val jobs = arrayListOf<Job>()
    
    // Injected dependencies available to all ability instances
    protected val plugin: SuperSmashMobsBrawl by inject()
    protected val twilight: Twilight by inject()

    override fun setup() {}

    override fun teardown() {
        listeners.forEach { it.unregister() }
        runnables.forEach { it.cancel() }
        jobs.forEach { it.cancel() }
    }

    abstract fun activate()

    open fun canActivate(): Boolean {
        if (isOnCooldown()) {
            player.sendMessage(
                mm("<red>${definition.name} is on cooldown for ${getRemainingCooldown()}s!</red>")
            )
            return false
        }

        return true
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

    protected fun setCooldown(time: Long = System.currentTimeMillis()) {
        lastUsed = time
    }
}
