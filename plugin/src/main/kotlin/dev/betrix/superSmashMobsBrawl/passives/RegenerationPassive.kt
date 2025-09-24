package dev.betrix.superSmashMobsBrawl.passives

import gg.flyte.twilight.scheduler.repeatingTask
import org.bukkit.attribute.Attribute
import org.bukkit.entity.Player

class RegenerationPassive(player: Player) : BrawlPassive("regeneration", player) {

    private val healAmount: Double = (metadata.double("healAmount") ?: 1.0)
    private val healIntervalTicks: Long = ((metadata.long("healIntervalTicks") ?: 60L)).coerceAtLeast(1L)

    override fun setup() {
        val regenTask =
            repeatingTask(healIntervalTicks) {
                if (!player.isOnline || player.isDead) {
                    cancel()
                    return@repeatingTask
                }

                val playerMaxHealth = player.getAttribute(Attribute.MAX_HEALTH)?.value ?: 20.0
                if (player.health < playerMaxHealth) {
                    val newHealth = (player.health + healAmount).coerceAtMost(playerMaxHealth)
                    player.health = newHealth
                }
            }
        runnables.add(regenTask)

        super.setup()
    }
}
