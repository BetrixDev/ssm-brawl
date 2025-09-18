package dev.betrix.superSmashMobsBrawl.minigames.managers

import dev.betrix.superSmashMobsBrawl.IManageable
import dev.betrix.superSmashMobsBrawl.Manageable
import dev.betrix.superSmashMobsBrawl.events.BrawlDeathEvent
import dev.betrix.superSmashMobsBrawl.events.DeathReason
import dev.betrix.superSmashMobsBrawl.events.HazardType
import dev.betrix.superSmashMobsBrawl.minigames.BrawlMinigame
import gg.flyte.twilight.scheduler.delay
import gg.flyte.twilight.scheduler.repeatingTask
import java.util.UUID
import org.bukkit.GameMode
import org.bukkit.entity.Player

interface IHazardManager : IManageable {}

class DefaultHazardManager(private val minigame: BrawlMinigame) : Manageable(), IHazardManager {
    private val recentHazardDeaths = mutableSetOf<UUID>()

    override fun setup() {
        val world = minigame.worldManager.getWorld() ?: return
        val voidLevel = world.data.voidLevel

        runnables.add(
            repeatingTask(5) {
                minigame.allPlayers().forEach { p ->
                    val mgWorld = world.world
                    minigame.allPlayers().forEach { p ->
                        val bp = p.player ?: return@forEach
                        if (bp.world != mgWorld) return@forEach
                        if (bp.gameMode != GameMode.SURVIVAL && bp.gameMode != GameMode.ADVENTURE)
                            return@forEach

                        if (bp.location.y <= voidLevel && recentHazardDeaths.add(bp.uniqueId)) {
                            handleHazardDeath(bp, HazardType.VOID)
                        }
                    }
                }
            }
        )

        runnables.add(
            repeatingTask(2) {
                minigame.allPlayers().forEach { player ->
                    if (player.isOnline && player.location?.block?.isLiquid == true) {
                        handleHazardDeath(player as Player, HazardType.LIQUID)
                    }
                }
            }
        )
    }

    private fun handleHazardDeath(player: Player, hazardType: HazardType) {
        BrawlDeathEvent.call(player, DeathReason.Hazard(hazardType))

        // small cooldown to prevent spam if they remain in the void
        val remover = delay(40) { recentHazardDeaths.remove(player.uniqueId) }
        runnables.add(remover)
    }
}
