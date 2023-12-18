package dev.betrix.supersmashmobsbrawl.passives

import com.github.shynixn.mccoroutine.bukkit.launch
import com.github.shynixn.mccoroutine.bukkit.ticks
import dev.betrix.supersmashmobsbrawl.SuperSmashMobsBrawl
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import org.bukkit.attribute.Attribute
import org.bukkit.entity.Player
import org.bukkit.event.HandlerList
import kotlin.math.min

class RegenerationPassive(
    private val player: Player,
    private val regenRate: Double,
    private val delayTicks: Int = 20
) : SSMBPassive() {
    private val plugin = SuperSmashMobsBrawl.instance

    private val regenerationJob: Job

    init {
        plugin.server.pluginManager.registerEvents(this, plugin)

        regenerationJob = plugin.launch {
            while (true) {
                if (!player.isDead && player.foodLevel > 0 && player.health != player.getAttribute(Attribute.GENERIC_MAX_HEALTH)?.value) {
                    player.health = min(player.health + regenRate, 20.0)
                    player.sendHealthUpdate()
                }

                delay(delayTicks.ticks)
            }
        }
    }

    override fun destroyPassive() {
        regenerationJob.cancel()
        HandlerList.unregisterAll(this)
    }
}