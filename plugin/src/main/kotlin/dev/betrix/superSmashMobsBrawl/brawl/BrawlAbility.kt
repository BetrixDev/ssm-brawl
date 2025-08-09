package dev.betrix.superSmashMobsBrawl.brawl

import dev.betrix.superSmashMobsBrawl.services.DataService
import dev.betrix.superSmashMobsBrawl.services.LangService
import gg.flyte.twilight.event.TwilightListener
import gg.flyte.twilight.scheduler.TwilightRunnable
import kotlinx.coroutines.Job
import org.bukkit.entity.Player
import org.bukkit.plugin.java.JavaPlugin
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

abstract class BrawlAbility(val id: String, val player: Player) : KoinComponent {
    protected val plugin: JavaPlugin by inject()
    private val dataService: DataService by inject()

    protected val abilityData =
        dataService.getAbility(id)
            ?: throw RuntimeException("No ability found in DataService with id $id")

    private var lastUsed: Long = 0
    protected val listeners = arrayListOf<TwilightListener>()
    protected val runnables = arrayListOf<TwilightRunnable>()
    protected val jobs = arrayListOf<Job>()
    private val lang: LangService by inject()

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
            val component =
                lang.t("messages.abilities.cooldown") {
                    "abilityId" to id
                    "seconds" to getRemainingCooldown()
                }
            player.sendMessage(component)
            return false
        }

        return true
    }

    fun isOnCooldown(): Boolean {
        val cooldownMs = abilityData.cooldown * 1000L
        return System.currentTimeMillis() - lastUsed < cooldownMs
    }

    fun getRemainingCooldown(): Int {
        val cooldownMs = (abilityData.cooldown * 1000).toLong()
        val elapsed = System.currentTimeMillis() - lastUsed
        return ((cooldownMs - elapsed) / 1000).coerceAtLeast(0).toInt()
    }

    protected fun setCooldown(time: Long = System.currentTimeMillis()) {
        lastUsed = time
    }
}
