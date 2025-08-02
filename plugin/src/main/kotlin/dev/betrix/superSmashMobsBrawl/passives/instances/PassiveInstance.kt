package dev.betrix.superSmashMobsBrawl.passives.instances

import dev.betrix.superSmashMobsBrawl.passives.definitions.PassiveDefinition
import gg.flyte.twilight.event.TwilightListener
import gg.flyte.twilight.scheduler.TwilightRunnable
import org.bukkit.entity.Player

abstract class PassiveInstance(val definition: PassiveDefinition, val player: Player) {
    protected val listeners = arrayListOf<TwilightListener>()
    protected val runnables = arrayListOf<TwilightRunnable>()
    
    abstract fun setup(): Unit

    open fun teardown() {
        listeners.forEach { it.unregister() }
        runnables.forEach { it.cancel() }
    }
}
