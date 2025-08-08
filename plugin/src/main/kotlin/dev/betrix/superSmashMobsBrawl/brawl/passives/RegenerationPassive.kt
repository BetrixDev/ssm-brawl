package dev.betrix.superSmashMobsBrawl.brawl.passives

import dev.betrix.superSmashMobsBrawl.brawl.BrawlPassive
import dev.betrix.superSmashMobsBrawl.passives.PassiveMetadata
import gg.flyte.twilight.scheduler.repeatingTask
import org.bukkit.attribute.Attribute
import org.bukkit.entity.Player

class RegenerationPassive(
    id: String,
    player: Player,
    metadata: PassiveMetadata,
    config: Map<String, Any?> = emptyMap(),
) : BrawlPassive(id, player, metadata, config) {

    private val healAmount = 1.0
    private val healIntervalTicks = 60L

    override fun setup() {
        val regenTask =
            repeatingTask(healIntervalTicks) {
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
