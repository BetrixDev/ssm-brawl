package dev.betrix.superSmashMobsBrawl.minigames

import com.github.michaelbull.result.onFailure
import dev.betrix.superSmashMobsBrawl.SuperSmashMobsBrawl
import dev.betrix.superSmashMobsBrawl.extensions.teleport
import dev.betrix.superSmashMobsBrawl.models.MinigamePlayer
import dev.betrix.superSmashMobsBrawl.models.MinigameState
import dev.betrix.superSmashMobsBrawl.models.MinigameTeam
import dev.betrix.superSmashMobsBrawl.models.brawlData.TeamBasedStocksMinigameDef
import dev.betrix.superSmashMobsBrawl.services.HubService
import dev.betrix.superSmashMobsBrawl.services.LangService
import dev.betrix.superSmashMobsBrawl.services.MinigameService
import gg.flyte.twilight.extension.feed
import gg.flyte.twilight.extension.heal
import org.bukkit.GameMode
import org.bukkit.entity.Player
import org.koin.core.component.inject

open class TeamBasedStocksMinigame(
    minigameId: String,
    gameId: String,
    private val teams: List<MinigameTeam>,
) :
    BrawlMinigame<TeamBasedStocksMinigameDef>(
        minigameId,
        gameId,
        teams.flatMap { it.players }.map { MinigamePlayer(it) },
    ) {

    private val hubService: HubService by inject()
    private val langService: LangService by inject()
    private val plugin: SuperSmashMobsBrawl by inject()
    private val minigameService: MinigameService by inject()

    private val playerToTeam: MutableMap<Player, MinigameTeam> = mutableMapOf()

    override suspend fun initMinigame(): com.github.michaelbull.result.Result<Unit, Exception> {
        // Initialize team stocks and mapping before calling base init (which teleports/assigns
        // kits)
        teams.forEach { team -> team.stocks = minigameData.stocks }
        teams.forEach { team -> team.players.forEach { playerToTeam[it] = team } }

        return super.initMinigame()
    }

    override suspend fun onPlayerDeath(player: Player) {
        if (state == MinigameState.ENDED) return

        val team = playerToTeam[player]
        if (team == null) {
            // Fallback: if somehow no team, just use base behavior
            super.onPlayerDeath(player)
            return
        }

        // Consume a stock for this team
        if (team.stocks > 0) {
            team.stocks -= 1
        }

        // If after consuming, team has zero stocks, the dying player is eliminated and should not
        // respawn
        if (team.stocks == 0) {
            eliminatePlayer(player)
            checkForWinnerAndEndIfNeeded()
            return
        }

        // Otherwise allow normal respawn flow
        super.onPlayerDeath(player)
        checkForWinnerAndEndIfNeeded()
    }

    override fun canPlayerLeaveMinigame(player: Player): Boolean {
        return true
    }

    override fun onPlayerLeave(player: Player) {
        // Cleanup kit/passives using base behavior
        super.onPlayerLeave(player)

        // Remove from team roster mapping
        playerToTeam[player]?.players?.remove(player)
        playerToTeam.remove(player)

        // Check if minigame should end due to insufficient players
        checkAndHandleMinigameEnd()
    }

    override fun checkAndHandleMinigameEnd() {
        if (state == MinigameState.ENDED) return
        super.checkAndHandleMinigameEnd()
        if (state == MinigameState.ENDED) return
        checkForWinnerAndEndIfNeeded()
    }

    private fun eliminatePlayer(player: Player) {
        // Put player into spectator mode at the spectator spawn and stop further participation
        if (brawlWorld == null) return

        player.teleport(brawlWorld!!.data.spectatorSpawnPoint)
        player.gameMode = GameMode.SPECTATOR
        player.allowFlight = true
        player.isFlying = true
        player.fallDistance = 0f
        player.feed()
        player.heal()

        // Ensure minigame kit effects are removed
        super.onPlayerLeave(player)
    }

    private fun checkForWinnerAndEndIfNeeded() {
        if (state == MinigameState.ENDED) return

        val teamsWithStocks = teams.filter { it.stocks > 0 }
        val teamsWithActivePlayer =
            teams.filter { team -> team.players.any { it.gameMode == GameMode.SURVIVAL } }

        if (teamsWithStocks.size == 1 && teamsWithActivePlayer.size <= 1) {
            endGame(teamsWithStocks.first())
        }
    }

    private fun endGame(winningTeam: MinigameTeam) {
        if (state == MinigameState.ENDED) return
        state = MinigameState.ENDED

        val winners = winningTeam.players.joinToString(", ") { it.name }.ifBlank { "Unknown" }
        val winMessage =
            langService.t("messages.minigames.teamBasedStocks.winner") {
                "winners" to winners
                "minigameId" to minigameId
            }

        // Announce to everyone online
        plugin.server.onlinePlayers.forEach { it.sendMessage(winMessage) }

        // Teleport all participants back to the hub
        players.forEach { p ->
            hubService.tryTeleportToDefaultHub(p.player).onFailure { /* ignore */ }
        }

        // Remove this minigame instance and cleanup
        minigameService.removeMinigameInstance(this)
        teardown()
    }
}
