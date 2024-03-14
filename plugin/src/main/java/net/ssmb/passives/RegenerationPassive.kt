package net.ssmb.passives

import com.github.shynixn.mccoroutine.bukkit.launch
import com.github.shynixn.mccoroutine.bukkit.ticks
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import net.ssmb.SSMB
import org.bukkit.attribute.Attribute
import org.bukkit.entity.Player
import kotlin.math.min

class RegenerationPassive(private val player: Player, meta: Map<String, String>) : IPassive {
    private val plugin = SSMB.instance
    private val regenDelayTicks = meta["regen_delay_ticks"]!!.toInt()
    private val regenRate = meta["regen_rate"]!!.toDouble()

    private var regenerationJob: Job? = null

    override fun createPassive() {
        regenerationJob = plugin.launch {
            while (true) {
                if (player.isDead) continue
                if (player.foodLevel <= 0) continue
                if (player.health != player.getAttribute(Attribute.GENERIC_MAX_HEALTH)?.value) continue

                player.health = min(player.health + regenRate, 20.0)
                player.sendHealthUpdate()

                delay(regenDelayTicks.ticks)
            }
        }
    }

    override fun destroyPassive() {
        regenerationJob?.cancel()
    }
}