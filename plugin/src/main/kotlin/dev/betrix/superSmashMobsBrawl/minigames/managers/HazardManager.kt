package dev.betrix.superSmashMobsBrawl.minigames.managers

import dev.betrix.superSmashMobsBrawl.Manageable
import dev.betrix.superSmashMobsBrawl.events.BrawlDeathEvent
import dev.betrix.superSmashMobsBrawl.events.DeathReason
import dev.betrix.superSmashMobsBrawl.minigames.BrawlMinigame
import gg.flyte.twilight.scheduler.repeatingTask
import gg.flyte.twilight.scheduler.delay
import org.bukkit.GameMode
import java.util.UUID

interface IHazardManager {
    fun initialize(minigame: BrawlMinigame)
}

class DefaultHazardManager : Manageable(), IHazardManager {
    private val recentVoidDeaths = mutableSetOf<UUID>()


    override fun initialize(minigame: BrawlMinigame) {
        val world = minigame.worldManager.getWorld() ?: return
        val voidLevel = world.data.voidLevel

        runnables.add(
            repeatingTask(5) {
                minigame.allPlayers().forEach { p ->
                                    val mgWorld = world.world
                                    minigame.allPlayers().forEach { p ->
                                            val bp = p.player ?: return@forEach
                                            if (!p.isOnline) return@forEach
                                           if (bp.world != mgWorld) return@forEach
                                            if (bp.gameMode != GameMode.SURVIVAL && bp.gameMode != GameMode.ADVENTURE) return@forEach

                                            if (bp.location.y <= voidLevel && recentVoidDeaths.add(bp.uniqueId)) {
                                                    BrawlDeathEvent.call(bp, DeathReason.Void)
                                                    // small cooldown to prevent spam if they remain in the void
                                                    val remover = delay(40) { recentVoidDeaths.remove(bp.uniqueId) }
                                                    runnables.add(remover)
                                                }
                                       }
                }
            }
        )
    }
}
