package dev.betrix.supersmashmobsbrawl.managers

import dev.betrix.supersmashmobsbrawl.SuperSmashMobsBrawl
import dev.betrix.supersmashmobsbrawl.managers.api.payloads.StartGame
import dev.betrix.supersmashmobsbrawl.minigames.SinglePlayerTestingGame
import org.bukkit.entity.Player

class GameManager {
    private val plugin = SuperSmashMobsBrawl.instance
    private val api = plugin.api

    suspend fun startGame(players: List<Player>, modeId: String, isRanked: Boolean) {
        // TODO: Eventually have an option to pass a map id in and not have it pick automatically
        val startGameResponse = api.tryStartGame(players, modeId, isRanked)

        if (startGameResponse is StartGame.Success) {
            val gameData = startGameResponse.value

            when (gameData.modeId) {
                "single_player_testing" -> SinglePlayerTestingGame(gameData)
                else -> throw RuntimeException("Unable to locate correct game id ${gameData.modeId}")
            }

        } else if (startGameResponse is StartGame.Error) {
            plugin.logger.info("Error creating game")
            // TODO: Handle error
        }
    }
}