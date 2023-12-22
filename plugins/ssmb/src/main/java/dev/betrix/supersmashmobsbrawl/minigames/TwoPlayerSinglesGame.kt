package dev.betrix.supersmashmobsbrawl.minigames

import com.github.shynixn.mccoroutine.bukkit.launch
import com.github.shynixn.mccoroutine.bukkit.ticks
import dev.betrix.supersmashmobsbrawl.SuperSmashMobsBrawl
import dev.betrix.supersmashmobsbrawl.enums.LangEntry
import dev.betrix.supersmashmobsbrawl.enums.SSMBGameState
import dev.betrix.supersmashmobsbrawl.kits.SSMBKit
import dev.betrix.supersmashmobsbrawl.managers.api.payloads.StartGameResponse
import dev.betrix.supersmashmobsbrawl.maps.GameMap
import kotlinx.coroutines.delay
import net.kyori.adventure.audience.Audience
import net.kyori.adventure.key.Key
import net.kyori.adventure.sound.Sound
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.title.Title
import org.bukkit.Bukkit
import org.bukkit.GameMode
import org.bukkit.Location
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.HandlerList
import org.bukkit.event.entity.PlayerDeathEvent
import org.bukkit.event.player.PlayerMoveEvent
import java.time.Duration
import java.util.*
import kotlin.random.Random

class TwoPlayerSinglesGame(private val gameData: StartGameResponse) : SSMBGame() {
    private val plugin = SuperSmashMobsBrawl.instance
    private val map = GameMap(gameData.gameId, gameData.map.mapId)
    private val generalAudience: Audience
    private val players = hashMapOf<Player, SSMBKit>()
    private val playerStocks = hashMapOf<Player, Int>()
    private var gameState = SSMBGameState.COUNTDOWN

    init {
        plugin.logger.info(
            "Attempting to start minigame with id ${gameData.gameId} and players ${gameData.players}"
        )

        plugin.server.pluginManager.registerEvents(this, plugin)

        try {
            gameData.players.forEachIndexed { playerIndex, data ->
                val player = Bukkit.getPlayer(UUID.fromString(data.uuid))
                    ?: throw RuntimeException("Fetching player of uuid ${data.uuid} returned null")

                players[player] = SSMBKit.kitFromId(data.kit.id, player, data.kit)

                val spawnPos = gameData.map.spawnLocations[playerIndex]
                map.teleportPlayer(
                    player,
                    Location(map.worldInstance, spawnPos.x.toDouble(), spawnPos.y.toDouble(), spawnPos.z.toDouble())
                )
            }
        } catch (e: Exception) {
            gameData.players.forEach {
                Bukkit.getPlayer(UUID.fromString(it.uuid)).let { plr ->
                    plr?.sendMessage("Unable to start the current game.")
                    plr?.teleport(plugin.hub.worldInstance.spawnLocation)
                }
            }
        }

        players.forEach { player ->
            playerStocks[player.key] = 4
        }

        generalAudience = Audience.audience(players.keys)

        plugin.launch {
            repeat(5) { i ->
                val times =
                    Title.Times.times(Duration.ofMillis(100), Duration.ofMillis(500), Duration.ofMillis(100))
                val mainTitle = Component.text(5 - i, NamedTextColor.GOLD)

                generalAudience.showTitle(Title.title(mainTitle, Component.empty(), times))
                generalAudience.playSound(
                    Sound.sound(
                        Key.key("block.note_block.hat"),
                        Sound.Source.AMBIENT,
                        1F,
                        1 * (i + 1 / 5).toFloat()
                    )
                )

                delay(20.ticks)
            }

            val times = Title.Times.times(
                Duration.ofMillis(100),
                Duration.ofMillis(500),
                Duration.ofMillis(100)
            )

            val mainTitle = Component.text("Start!", NamedTextColor.GOLD)

            generalAudience.showTitle(Title.title(mainTitle, Component.empty(), times))
            generalAudience.playSound(
                Sound.sound(
                    Key.key("block.note_block.chime"),
                    Sound.Source.AMBIENT,
                    1F,
                    2F
                )
            )
        }
    }

    @EventHandler
    fun onPlayerMove(event: PlayerMoveEvent) {
        if (players[event.player] == null) return

        if (gameState == SSMBGameState.COUNTDOWN) {
            event.isCancelled = true
            return
        }
    }

    @EventHandler
    fun onPlayerDeath(event: PlayerDeathEvent) {
        if (playerStocks[event.player] == null) return

        val player = event.player
        val playerKit = players[player]!!

        event.isCancelled = true
        player.gameMode = GameMode.SPECTATOR
        playerKit.destroyKit()

        val currentStocks = playerStocks[event.player]!!

        playerStocks[event.player] = currentStocks - 1

        if (currentStocks - 1 != 0) {
            val times = Title.Times.times(
                Duration.ofMillis(100),
                Duration.ofMillis(4800),
                Duration.ofMillis(100)
            )

            val mainTitle = plugin.lang.getComponent(LangEntry.MINIGAME_RESPAWNING)

            player.showTitle(Title.title(mainTitle, Component.empty(), times))

            plugin.launch {
                delay((5 * 20).ticks)
                player.gameMode = GameMode.SURVIVAL
                playerKit.equipKit()
                player.teleport(getRandomSpawnLocation())
            }
        } else {
            player.teleport(Location(map.worldInstance, 0.0, 100.0, 0.0))

            if (shouldGameEnd()) {
                endGame()
            }
        }
    }

    private fun shouldGameEnd(): Boolean {
        val playersWithStocksLeft = playerStocks.filter { it.value > 0 }

        return playersWithStocksLeft.size == 1
    }

    private fun endGame() {
        HandlerList.unregisterAll(this)
    }

    private fun getRandomSpawnLocation(): Location {
        val cursor = Random.nextInt(0, gameData.map.spawnLocations.size)
        val location = gameData.map.spawnLocations[cursor]

        return Location(
            map.worldInstance,
            location.x.toDouble(),
            location.y.toDouble(),
            location.z.toDouble()
        )
    }
}