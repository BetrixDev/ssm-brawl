package dev.betrix.superSmashMobsBrawl.minigames

import com.github.michaelbull.result.Err
import com.github.michaelbull.result.Ok
import com.github.michaelbull.result.Result
import com.github.michaelbull.result.onFailure
import com.github.michaelbull.result.onSuccess
import dev.betrix.superSmashMobsBrawl.Manageable
import dev.betrix.superSmashMobsBrawl.kits.instances.KitInstance
import dev.betrix.superSmashMobsBrawl.models.BrawlGameWorld
import dev.betrix.superSmashMobsBrawl.models.MinigameState
import dev.betrix.superSmashMobsBrawl.models.brawlData.MinigameDef
import dev.betrix.superSmashMobsBrawl.services.AssignKitError
import dev.betrix.superSmashMobsBrawl.services.DataService
import dev.betrix.superSmashMobsBrawl.services.KitService
import gg.flyte.twilight.extension.kill
import gg.flyte.twilight.scheduler.repeatingTask
import org.bukkit.entity.Player
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

abstract class BrawlMinigame<TMinigameDef : MinigameDef>(
    minigameId: String,
    gameId: String,
    protected val brawlWorld: BrawlGameWorld,
    protected val players: List<Player>,
) : Manageable(), KoinComponent {
    protected val dataService: DataService by inject()
    private val kitService: KitService by inject()

    protected val minigameData = (dataService.getMinigame(minigameId) as TMinigameDef?) ?: throw RuntimeException("")
    protected val assignedKits = mutableListOf<Pair<Player, KitInstance>>()

    var state = MinigameState.PREFLIGHT
        protected set

    open suspend fun initMinigame(): Result<Unit, Exception> {
        val voidLevel = brawlWorld.data.voidLevel

        runnables.add(
            repeatingTask(5) {
                players.forEach { player ->
                    if (player.location.y <= voidLevel) {
                        player.kill()
                    }
                }
            }
        )

        players.forEach { player ->
            kitService
                .assignKit(player)
                .onFailure { err ->
                    return when (err) {
                        AssignKitError.PLAYER_HAS_KIT ->
                            Err(
                                RuntimeException(
                                    "Player $player already has a kit assigned to them"
                                )
                            )
                    }
                }
                .onSuccess { kit -> assignedKits.add(Pair(player, kit)) }
        }

        state = MinigameState.STARTING

        return Ok(Unit)
    }
}
