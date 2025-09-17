package dev.betrix.superSmashMobsBrawl.minigames.managers

import dev.betrix.superSmashMobsBrawl.IManageable
import dev.betrix.superSmashMobsBrawl.Manageable
import dev.betrix.superSmashMobsBrawl.events.BrawlDeathEvent
import dev.betrix.superSmashMobsBrawl.events.DeathReason
import dev.betrix.superSmashMobsBrawl.minigames.BrawlMinigame
import gg.flyte.twilight.scheduler.delay
import gg.flyte.twilight.scheduler.repeatingTask
import java.util.UUID
import org.bukkit.GameMode

interface IHazardManager : IManageable {}

class DefaultHazardManager(private val minigame: BrawlMinigame) : Manageable(), IHazardManager {
    private val recentVoidDeaths = mutableSetOf<UUID>()

    override fun setup() {
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
                        if (bp.gameMode != GameMode.SURVIVAL && bp.gameMode != GameMode.ADVENTURE)
                            return@forEach

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
