package dev.betrix.supersmashmobsbrawl.passives

import com.github.shynixn.mccoroutine.bukkit.launch
import com.github.shynixn.mccoroutine.bukkit.ticks
import dev.betrix.supersmashmobsbrawl.SuperSmashMobsBrawl
import dev.betrix.supersmashmobsbrawl.enums.LangEntry
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.HandlerList
import org.bukkit.event.entity.EntityDamageByEntityEvent
import org.bukkit.event.entity.EntityDamageEvent
import kotlin.math.max
import kotlin.math.min

class HungerPassive(
    private val player: Player,
    private val secondsToDrain: Double,
    private val hungerRestoreDelay: Long
) : SSMBPassive() {
    constructor(player: Player) : this(player, 10.0)
    constructor(player: Player, secondsToDrain: Double) : this(player, secondsToDrain, 250)

    private val plugin = SuperSmashMobsBrawl.instance
    private val hungerJob: Job

    private var hungerTicks: Long = 0
    private var lastHungerRestore = System.currentTimeMillis()

    init {
        plugin.server.pluginManager.registerEvents(this, plugin)

        hungerJob = plugin.launch {
            while (true) {
                hungerTicks = (hungerTicks + 1) % 10
                player.saturation = 3F
                player.exhaustion = 0F

                if (player.foodLevel <= 0) {
                    plugin.lang.sendToPlayer(LangEntry.PASSIVE_HUNGER_CTA, player)

                    val damageEvent = EntityDamageEvent(player, EntityDamageEvent.DamageCause.STARVATION, 1.0)
                    damageEvent.callEvent()
                }

                if (hungerTicks == 0.toLong()) {
                    player.foodLevel = max(0, player.foodLevel - 1)
                }

                delay(20.ticks)
            }
        }
    }

    override fun destroyPassive() {
        hungerJob.cancel()
        HandlerList.unregisterAll(this)
    }

    @EventHandler
    fun onEntityDamageByEntity(event: EntityDamageByEntityEvent) {
        if (event.damager != player) return

        if (System.currentTimeMillis() - lastHungerRestore < hungerRestoreDelay) {
            return
        }

        lastHungerRestore = System.currentTimeMillis()

        val amount = max(1.0, event.damage / 2)
        player.foodLevel = min(20.0, player.foodLevel + amount).toInt()
    }
}