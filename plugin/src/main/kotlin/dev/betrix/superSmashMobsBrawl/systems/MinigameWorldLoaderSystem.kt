package dev.betrix.superSmashMobsBrawl.systems

import com.github.michaelbull.result.onFailure
import com.github.michaelbull.result.onSuccess
import com.github.quillraven.fleks.Entity
import com.github.quillraven.fleks.IteratingSystem
import com.github.quillraven.fleks.World.Companion.family
import com.github.quillraven.fleks.World.Companion.inject
import com.github.shynixn.mccoroutine.bukkit.launch
import dev.betrix.superSmashMobsBrawl.SuperSmashMobsBrawl
import dev.betrix.superSmashMobsBrawl.components.MinigameComponent
import dev.betrix.superSmashMobsBrawl.components.MinigameState
import dev.betrix.superSmashMobsBrawl.models.BrawlWorld
import dev.betrix.superSmashMobsBrawl.models.brawlData.GameMapDef
import dev.betrix.superSmashMobsBrawl.models.brawlData.MinigameDef
import dev.betrix.superSmashMobsBrawl.services.DataService
import dev.betrix.superSmashMobsBrawl.services.WorldService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.withContext

class MinigameWorldLoaderSystem(
    private val plugin: SuperSmashMobsBrawl = inject(),
    private val dataService: DataService = inject(),
    private val worldService: WorldService = inject(),
) : IteratingSystem(family { all(MinigameComponent) }) {
    private val worldLoadingJobs = mutableMapOf<Entity, Job>()

    override fun onTickEntity(entity: Entity) {
        val minigame = entity[MinigameComponent]

        if (minigame.state != MinigameState.LOADING_WORLD || worldLoadingJobs.containsKey(entity)) {
            return
        }

        plugin.logger.info("Loading world for minigame (${minigame.instanceId})")

        val worldToLoad = getWorldToLoad(minigame.minigame)

        worldLoadingJobs[entity] =
            plugin.launch {
                withContext(Dispatchers.IO) {
                    worldService
                        .copyAndLoadWorld(worldToLoad, minigame.instanceId)
                        .onSuccess {
                            minigame.loadedWorld = it
                            // Advance state so other systems (e.g. start/teleport) can continue
                            minigame.state = MinigameState.STARTING
                        }
                        .onFailure { err ->
                            plugin.logger.severe(
                                "Failed to load world for minigame (${minigame.instanceId}): ${'$'}{err.message}"
                            )
                            minigame.state = MinigameState.CLEANUP
                        }
                }
            }
    }

    private fun getWorldToLoad(minigameDef: MinigameDef): GameMapDef {
        val validMaps =
            dataService.getAllGameMaps().filter { map ->
                minigameDef.mapWhitelist?.let { whitelist ->
                    return@filter whitelist.contains(map.id)
                }

                minigameDef.mapBlacklist?.let { blacklist ->
                    return@filter !blacklist.contains(map.id)
                }

                true
            }

        if (validMaps.isEmpty()) {
            plugin.logger.severe(
                "Minigame (${minigameDef.id}) had no valid maps, picking a default"
            )
            return dataService.getAllGameMaps().first()
        }

        return validMaps.random()
    }

    private suspend fun loadWorld(
        gameMap: GameMapDef,
        afterLoad: (result: Result<BrawlWorld>) -> Unit,
    ) {}
}
