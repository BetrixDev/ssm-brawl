package dev.betrix.superSmashMobsBrawl.minigames.managers

import com.github.michaelbull.result.Err
import com.github.michaelbull.result.Result
import com.github.michaelbull.result.map
import dev.betrix.superSmashMobsBrawl.IManageable
import dev.betrix.superSmashMobsBrawl.Manageable
import dev.betrix.superSmashMobsBrawl.minigames.BrawlMinigame
import dev.betrix.superSmashMobsBrawl.models.BrawlGameWorld
import dev.betrix.superSmashMobsBrawl.services.DataService
import dev.betrix.superSmashMobsBrawl.services.WorldService
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

/** Responsible for selecting, loading, and cleaning up the game world for a minigame. */
interface IWorldManager : IManageable {
    suspend fun loadWorld(gameId: String): Result<BrawlGameWorld, Exception>

    fun getWorld(): BrawlGameWorld?
}

class DefaultWorldManager(private val minigame: BrawlMinigame) :
    Manageable(), IWorldManager, KoinComponent {
    private val worldService: WorldService by inject()
    private val dataService: DataService by inject()

    private var brawlWorld: BrawlGameWorld? = null

    override suspend fun loadWorld(gameId: String): Result<BrawlGameWorld, Exception> {
        val validMaps =
            dataService.getAllGameMaps().filter { map ->
                minigame.minigameDef.mapWhitelist?.let { whitelist ->
                    return@filter whitelist.contains(map.id)
                }
                minigame.minigameDef.mapBlacklist?.let { blacklist ->
                    return@filter !blacklist.contains(map.id)
                }
                true
            }

        val selectedMap =
            validMaps.randomOrNull()
                ?: return Err(
                    RuntimeException("No valid maps found for ${minigame.minigameDef.id}")
                )

        try {
            val result =
                worldService.copyAndLoadWorld(selectedMap, gameId).map { it as BrawlGameWorld }
            result.map { loaded -> brawlWorld = loaded }
            return result
        } catch (e: Exception) {
            return Err(e)
        }
    }

    override fun getWorld(): BrawlGameWorld? = brawlWorld

    override fun teardown() {
        super.teardown()
        // WorldService handles unloading via its own lifecycle; nothing to do here yet.
        brawlWorld = null
    }
}
