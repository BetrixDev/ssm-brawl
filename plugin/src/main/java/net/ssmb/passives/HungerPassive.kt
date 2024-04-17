package net.ssmb.passives

import com.github.shynixn.mccoroutine.bukkit.launch
import com.github.shynixn.mccoroutine.bukkit.ticks
import kotlin.math.max
import kotlin.math.min
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import net.ssmb.SSMB
import net.ssmb.dtos.minigame.MinigameStartSuccess
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.HandlerList
import org.bukkit.event.Listener
import org.bukkit.event.entity.EntityDamageByEntityEvent
import org.bukkit.event.entity.EntityDamageEvent

class HungerPassive(
    private val player: Player,
    passiveData: MinigameStartSuccess.PlayerData.KitData.PassiveEntry.PassiveData
) : IPassive, Listener {
    private val plugin = SSMB.instance
    private val secondsToDrain = passiveData.meta?.get("seconds_to_drain")!!.toDouble()
    private val hungerRestoreDelay = passiveData.meta?.get("hunger_restore_delay")!!.toInt()

    private var hungerJob: Job? = null
    private var hungerTicks: Long = 0
    private var lastHungerRestore = System.currentTimeMillis()

    override fun createPassive() {
        plugin.server.pluginManager.registerEvents(this, plugin)

        hungerJob =
            plugin.launch {
                while (true) {
                    hungerTicks++

                    player.saturation = 3f
                    player.exhaustion = 0f

                    if (player.foodLevel <= 0) {
                        val damageEvent =
                            EntityDamageEvent(player, EntityDamageEvent.DamageCause.STARVATION, 1.0)
                        damageEvent.callEvent()
                    }

                    if ((hungerTicks % 10).toInt() == 0) {
                        player.foodLevel = max(0, player.foodLevel - 1)
                    }

                    delay(20.ticks)
                }
            }
    }

    override fun destroyPassive() {
        HandlerList.unregisterAll(this)

        hungerJob?.cancel()
    }

    @EventHandler
    fun onEntityDamageByEntity(event: EntityDamageByEntityEvent) {
        if (event.damager != player) return

        val currentTime = System.currentTimeMillis()

        if (currentTime - lastHungerRestore < hungerRestoreDelay) return

        lastHungerRestore = currentTime

        val amount = max(1.0, event.damage / 2)
        player.foodLevel = min(20.0, player.foodLevel + amount).toInt()
    }
}
