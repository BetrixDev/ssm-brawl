package dev.betrix.superSmashMobsBrawl.minigames.managers

import dev.betrix.superSmashMobsBrawl.Manageable
import dev.betrix.superSmashMobsBrawl.events.BrawlDeathEvent
import dev.betrix.superSmashMobsBrawl.events.DeathReason
import dev.betrix.superSmashMobsBrawl.minigames.BrawlMinigame
import gg.flyte.twilight.scheduler.repeatingTask
import org.bukkit.GameMode

interface IHazardManager {
    fun initialize(minigame: BrawlMinigame)
}

class DefaultHazardManager : Manageable(), IHazardManager {
    override fun initialize(minigame: BrawlMinigame) {
        val world = minigame.worldManager.getWorld() ?: return
        val voidLevel = world.data.voidLevel

        runnables.add(
            repeatingTask(5) {
                minigame.allPlayers().forEach { p ->
                    if (p.isOnline && p.player!!.gameMode == GameMode.SURVIVAL) {
                        if (p.player!!.location.y <= voidLevel) {
                            BrawlDeathEvent.call(p.player!!, DeathReason.Void)
                        }
                    }
                }
            }
        )
    }
}
