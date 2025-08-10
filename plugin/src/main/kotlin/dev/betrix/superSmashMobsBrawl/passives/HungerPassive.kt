package dev.betrix.superSmashMobsBrawl.passives

import dev.betrix.superSmashMobsBrawl.events.Damager
import dev.betrix.superSmashMobsBrawl.events.SmashDamageEvent
import dev.betrix.superSmashMobsBrawl.utils.mm
import gg.flyte.twilight.event.event
import gg.flyte.twilight.extension.feed
import gg.flyte.twilight.scheduler.repeatingTask
import kotlin.math.max
import kotlin.math.min
import org.bukkit.GameMode
import org.bukkit.entity.Player
import org.bukkit.event.entity.PlayerDeathEvent

class HungerPassive(player: Player) : BrawlPassive("hunger", player) {
    private val hungerRestoreDelayMs = 250L
    private var hungerTicks = 0L
    private var lastHungerRestoreMs = System.currentTimeMillis()

    override fun setup() {
        val damageListener =
            event<SmashDamageEvent> {
                val isThisPlayerDamager =
                    when (damager) {
                        is Damager.DamagerLivingEntity ->
                            damager.livingEntity == this@HungerPassive.player
                        is Damager.System -> false
                        null -> false
                    }

                if (!isThisPlayerDamager) return@event

                hungerRestore(damage)
            }
        listeners.add(damageListener)

        val deathListener =
            event<PlayerDeathEvent> {
                if (player != this@HungerPassive.player) return@event
                player.feed()
                lastHungerRestoreMs = System.currentTimeMillis()
            }
        listeners.add(deathListener)

        val hungerTask = repeatingTask(10) { activate() }
        runnables.add(hungerTask)

        super.setup()
    }

    private fun activate() {
        if (player.gameMode != GameMode.SPECTATOR) {
            return
        }

        hungerTicks = (hungerTicks + 1) % 10

        player.saturation = 3f
        player.exhaustion = 0f

        if (player.foodLevel <= 0) {
            player.sendMessage(mm("<red>Attack other players to restore hunger!</red>"))

            val damageEvent =
                SmashDamageEvent(
                    victim = player,
                    damager = Damager.System,
                    damage = 1.0,
                    knockbackMultiplier = 0.0,
                )
            damageEvent.callEvent()
            return
        }

        if (hungerTicks == 0L) {
            player.foodLevel = max(0, player.foodLevel - 1)
        }
    }

    private fun hungerRestore(damage: Double) {
        if ((System.currentTimeMillis() - lastHungerRestoreMs) < hungerRestoreDelayMs) {
            return
        }

        lastHungerRestoreMs = System.currentTimeMillis()

        val amount = max(1, (damage / 2).toInt())
        player.foodLevel = min(20, player.foodLevel + amount)
    }

    override fun teardown() {
        player.feed()
        super.teardown()
    }
}
