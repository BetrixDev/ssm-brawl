package dev.betrix.superSmashMobsBrawl.abilities.instances

import dev.betrix.superSmashMobsBrawl.SuperSmashMobsBrawl
import dev.betrix.superSmashMobsBrawl.abilities.definitions.AbilityDefinition
import dev.betrix.superSmashMobsBrawl.utils.mm
import gg.flyte.twilight.event.TwilightListener
import gg.flyte.twilight.scheduler.TwilightRunnable
import kotlinx.coroutines.Job
import org.bukkit.entity.Player
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import dev.betrix.superSmashMobsBrawl.services.DataService

abstract class AbilityInstance(val definition: AbilityDefinition, val player: Player) : KoinComponent {
    protected val plugin = SuperSmashMobsBrawl.instance
    protected val dataService: DataService by inject()
    private var lastUsed: Long = 0
    protected val listeners = arrayListOf<TwilightListener>()
    protected val runnables = arrayListOf<TwilightRunnable>()
    protected val jobs = arrayListOf<Job>()

    open fun setup() {}

    open fun teardown() {
        listeners.forEach { it.unregister() }
        runnables.forEach { it.cancel() }
        jobs.forEach { it.cancel() }
    }

    open fun activate() {
        setCooldown()
    }

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
