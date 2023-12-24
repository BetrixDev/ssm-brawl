package dev.betrix.supersmashmobsbrawl.managers

import dev.betrix.supersmashmobsbrawl.SuperSmashMobsBrawl
import dev.betrix.supersmashmobsbrawl.managers.api.payloads.StartGame
import dev.betrix.supersmashmobsbrawl.minigames.SSMBGame
import dev.betrix.supersmashmobsbrawl.minigames.SinglePlayerTestingGame
import dev.betrix.supersmashmobsbrawl.minigames.TwoPlayerSinglesGame
import org.bukkit.entity.Player

class GameManager {
    private val plugin = SuperSmashMobsBrawl.instance
    private val api = plugin.api

    val ongoingGames = arrayListOf<SSMBGame>()

    suspend fun startGame(players: List<Player>, modeId: String, isRanked: Boolean) {
        // TODO: Eventually have an option to pass a map id in and not have it pick automatically
        val startGameResponse = api.tryStartGame(players, modeId, isRanked)

        if (startGameResponse is StartGame.Success) {
            val gameData = startGameResponse.value

            val game = when (gameData.modeId) {
                "single_player_testing" -> SinglePlayerTestingGame(gameData)
                "two_player_singles" -> TwoPlayerSinglesGame(gameData)
                else -> throw RuntimeException("Unable to locate correct game id ${gameData.modeId}")
            }

            ongoingGames.add(game)

        } else if (startGameResponse is StartGame.Error) {
            plugin.logger.info("Error creating game")
            // TODO: Handle error
        }
    }


}