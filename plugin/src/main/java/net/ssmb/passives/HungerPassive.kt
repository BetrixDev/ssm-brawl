package net.ssmb.passives

import com.github.shynixn.mccoroutine.bukkit.launch
import com.github.shynixn.mccoroutine.bukkit.ticks
import kotlin.math.max
import kotlin.math.min
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import net.ssmb.SSMB
import net.ssmb.dtos.minigame.MinigameStartSuccess
import net.ssmb.utils.t
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.HandlerList
import org.bukkit.event.Listener
import org.bukkit.event.entity.EntityDamageByEntityEvent
import org.bukkit.event.entity.EntityDamageEvent

class HungerPassive(
    private val player: Player,
    passiveData: MinigameStartSuccess.PlayerData.KitData.PassiveEntry.PassiveData
) : SsmbPassive(player, passiveData) {
    private val plugin = SSMB.instance
    
    private val hungerRestoreDelay = getMetaInt("hunger_restore_delay", 250)

    private var hungerJob: Job? = null
    private var hungerTicks: Long = 0
    private var lastHungerRestore = System.currentTimeMillis()

    override fun initializePassive() {
        plugin.server.pluginManager.registerEvents(this, plugin)

        hungerJob =
            plugin.launch {
                while (true) {
                    hungerTicks = (hungerTicks + 1) % 10

                    player.saturation = 3f
                    player.exhaustion = 0f

                    if (player.foodLevel <= 0) {
                        val damageEvent =
                            EntityDamageEvent(player, EntityDamageEvent.DamageCause.STARVATION, 1.0)
                        damageEvent.callEvent()
                        player.sendActionBar(t("passive.hungerNotice"))
                    }

                    if (hungerTicks.toInt() == 0) {
                        player.foodLevel = 0.coerceAtLeast(player.foodLevel - 1)
                    }

                    delay(1.ticks)
                }
            }
    }

    override fun destroyPassive() {
        super.destroyPassive()
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
