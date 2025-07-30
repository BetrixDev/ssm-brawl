package dev.betrix.superSmashMobsBrawl.minigames.instances

import com.github.michaelbull.result.Err
import com.github.michaelbull.result.Ok
import com.github.michaelbull.result.Result
import com.github.michaelbull.result.onFailure
import com.github.shynixn.mccoroutine.bukkit.minecraftDispatcher
import dev.betrix.superSmashMobsBrawl.SuperSmashMobsBrawl
import dev.betrix.superSmashMobsBrawl.extensions.getEquidistant
import dev.betrix.superSmashMobsBrawl.minigames.MinigameState
import dev.betrix.superSmashMobsBrawl.minigames.definitions.MinigameDefinition
import dev.betrix.superSmashMobsBrawl.models.MinigameTeam
import dev.betrix.superSmashMobsBrawl.services.AssignKitError
import dev.betrix.superSmashMobsBrawl.services.KitService
import dev.betrix.superSmashMobsBrawl.utils.createLocation
import dev.betrix.superSmashMobsBrawl.utils.mm
import gg.flyte.twilight.extension.feed
import gg.flyte.twilight.extension.heal
import gg.flyte.twilight.extension.resetFlySpeed
import gg.flyte.twilight.extension.resetWalkSpeed
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import net.kyori.adventure.text.Component
import net.kyori.adventure.title.Title
import org.bukkit.GameMode
import org.bukkit.Sound
import org.bukkit.util.Vector
import java.time.Duration
import kotlin.time.Duration.Companion.seconds

class TwoPlayerDuelsMinigameInstance(definition: MinigameDefinition, teams: List<MinigameTeam>): MinigameInstance(definition, teams) {
    private val plugin = SuperSmashMobsBrawl.instance

    private val countdownSeconds = 5

    override suspend fun initMinigame(): Result<Unit, Exception> {
        super.initMinigame().onFailure { return Err(it) }

        val spawnPoints = map.spawnPoints.getEquidistant(players.size)

        teams.forEachIndexed { teamIdx, team ->
            team.players.forEachIndexed { playerIdx, player  ->
                player.feed()
                player.heal()
                player.gameMode = GameMode.SURVIVAL

                // Freeze player
                player.walkSpeed = 0f
                player.flySpeed = 0f
                player.velocity = Vector(0, 0, 0)

                player.teleport(createLocation(world, spawnPoints[(teamIdx + 1) * playerIdx]))

                KitService.assignKit(player).onFailure {
                    when (it) {
                        AssignKitError.PLAYER_HAS_KIT -> return Err(RuntimeException("Player $player already has a kit assigned to them"))
                    }
                }
            }
        }

        state = MinigameState.STARTING

        doCountdown()

        players.forEach { player ->
            player.resetWalkSpeed()
        }

        state = MinigameState.ONGOING

        return Ok(Unit)
    }

    override suspend fun teardownMinigame() {
        super.teardownMinigame()

        players.forEach { player ->
            player.resetFlySpeed()
            player.resetWalkSpeed()
            KitService.unassignKit(player)
        }
    }

    override fun shouldEndMinigame(): Boolean {
        teams.forEach {
            if (it.players.isEmpty()) {
                return true
            }
        }

        return false
    }

    override suspend fun onMinigameEnd() {
        val winningTeam = teams.first { it.stocks > 0 }

        winningTeam.players.forEach {
            it.sendMessage("You won!")
        }

        super.onMinigameEnd()
    }

    private suspend fun doCountdown() {
        withContext(plugin.minecraftDispatcher) {
            repeat(countdownSeconds) { iteration ->
                playPingSound()

                val title = Title.title(
                    mm("<green>${iteration + 1}</green>"),
                    mm("<gray>Game starting in</gray>"),
                    Title.Times.times(
                        Duration.ofMillis(250),
                        Duration.ofMillis(500),
                        Duration.ofMillis(250),
                    )
                )

                world.showTitle(title)

                withContext(Dispatchers.IO) {
                    delay(1.seconds)
                }
            }

            playPingSound()

            val startTitle = Title.title(
                mm("<green>Go!</green>"),
                Component.empty()
            )

            world.showTitle(startTitle)
        }
    }

    private fun playPingSound() {
        players.forEach { player ->
            player.playSound(player.location, Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1f, 1f)
        }
    }
}