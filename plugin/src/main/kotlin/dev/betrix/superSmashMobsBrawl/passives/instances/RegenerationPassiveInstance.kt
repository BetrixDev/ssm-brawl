package dev.betrix.superSmashMobsBrawl.passives.instances

import dev.betrix.superSmashMobsBrawl.passives.definitions.PassiveDefinition
import gg.flyte.twilight.scheduler.repeatingTask
import org.bukkit.attribute.Attribute
import org.bukkit.entity.Player

class RegenerationPassiveInstance(definition: PassiveDefinition, player: Player) :
    PassiveInstance(definition, player) {

    private val healAmount = 1.0 // Half a heart
    private val healIntervalTicks = 60L // 3 seconds (20 ticks per second)

    override fun setup() {
        // Start regeneration task
        val regenTask =
            repeatingTask(healIntervalTicks) {
                if (player.health < (player.getAttribute(Attribute.MAX_HEALTH)?.value ?: 20.0)) {
                    val newHealth = (player.health + healAmount).coerceAtMost(player.maxHealth)
                    player.health = newHealth
                }
            }
        runnables.add(regenTask)

        super.setup()
    }
}
