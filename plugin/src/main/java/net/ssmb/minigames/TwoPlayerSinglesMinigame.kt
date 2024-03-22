package net.ssmb.minigames

import com.github.shynixn.mccoroutine.bukkit.launch
import com.github.shynixn.mccoroutine.bukkit.registerSuspendingEvents
import com.github.shynixn.mccoroutine.bukkit.ticks
import io.papermc.paper.entity.LookAnchor
import kotlinx.coroutines.delay
import net.kyori.adventure.key.Key
import net.kyori.adventure.sound.Sound
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.title.Title
import net.ssmb.SSMB
import net.ssmb.dtos.minigame.MinigameStartSuccess
import net.ssmb.enums.MinigameState
import net.ssmb.kits.IKit
import net.ssmb.kits.constructKitFromData
import net.ssmb.utils.Atom
import org.bukkit.GameMode
import org.bukkit.Location
import org.bukkit.World
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.entity.PlayerDeathEvent
import org.bukkit.util.Vector
import java.time.Duration

class TwoPlayerSinglesMinigame(
    private val players: List<Player>,
    private val minigameData: MinigameStartSuccess
) : IMinigame, Listener {
    private val plugin = SSMB.instance
    private val minigameState = Atom(MinigameState.LOADING)
    private lateinit var minigameWorld: World
    private val playerKits = hashMapOf<Player, IKit>()
    private val teamsStocks = hashMapOf<List<Player>, Int>()

    init {
        plugin.server.pluginManager.registerSuspendingEvents(this, plugin)
    }

    override suspend fun initializeMinigame() {
        minigameState.subscribe { state, _ ->
            when (state) {
                MinigameState.LOADING -> doMinigameLoading()
                MinigameState.COUNTDOWN -> doMinigameCountdown()
                MinigameState.RUNNING -> doMinigameRunning()
                MinigameState.ENDING -> doMinigameEnd()
                else -> {}
            }
        }

    }

    private fun doMinigameLoading() {
        minigameWorld = plugin.worlds.createSsmbWorld(minigameData.map.id, minigameData.gameId)

        players.forEachIndexed { idx, it ->
            val spawnCoords = minigameData.map.spawnPoints[idx]
            val tpLocation = Location(minigameWorld, spawnCoords.x, spawnCoords.y, spawnCoords.z)
            it.teleport(tpLocation)

            it.walkSpeed = 0.0f
            it.lookAt(minigameWorld.spawnLocation, LookAnchor.EYES)


            val playerData = minigameData.players.find { itt -> itt.uuid == it.uniqueId.toString() }!!
            val kit = constructKitFromData(it, playerData.selectedKit)
            kit.initializeKit()

            teamsStocks[listOf(it)] = minigameData.minigame.stocks

            playerKits[it] = kit
        }

        minigameState.set(MinigameState.COUNTDOWN)
    }

    private fun doMinigameCountdown() {
        val titleTimes = Title.Times.times(
            Duration.ofMillis(100),
            Duration.ofMillis(500),
            Duration.ofMillis(100)
        )

        plugin.launch {
            repeat(5) {

                val title = Title.title(
                    Component.text("Starting in ${5 - it}", NamedTextColor.GOLD),
                    Component.empty(),
                    titleTimes
                )

                minigameWorld.showTitle(title)
                minigameWorld.playSound(
                    Sound.sound(
                        Key.key("block.note_block.hat"),
                        Sound.Source.AMBIENT,
                        1f,
                        1 * (1 + 1 / 5).toFloat()
                    )
                )

                delay(20.ticks)
            }

            val startTitle = Title.title(
                Component.text("Start!", NamedTextColor.GOLD),
                Component.empty(),
                titleTimes
            )

            minigameWorld.showTitle(startTitle)
            minigameWorld.playSound(
                Sound.sound(
                    Key.key("block.note_block.chime"),
                    Sound.Source.AMBIENT,
                    1f,
                    2f
                )
            )

            minigameState.set(MinigameState.RUNNING)
        }
    }

    private fun doMinigameRunning() {
        players.forEach {
            it.walkSpeed = 0.2f
        }
    }

    private fun doMinigameEnd() {
        val winningPlayers = teamsStocks.entries.first { it.value > 0 }.key
        val losingPlayers = teamsStocks.entries.filter { it.value == 0 }

        winningPlayers.forEach {
            it.sendMessage(Component.text("You won!", NamedTextColor.GREEN))
        }

        losingPlayers.forEach {
            it.key.forEach { plr ->
                plr.sendMessage(Component.text("You lost!", NamedTextColor.RED))
            }
        }
    }

    private fun checkShouldMinigameEnd() {
        val teamsWithStocksLeft = teamsStocks.filter { it.value > 0 }

        if (teamsWithStocksLeft.size == 1) {
            minigameState.set(MinigameState.ENDING)
        }
    }

    @EventHandler
    suspend fun onPlayerDeath(event: PlayerDeathEvent) {
        val player = event.entity

        if (!players.contains(player)) return

        event.isCancelled = true

        playerKits[player]!!.destroyKit()

        val spectatorSpawnCoords = minigameWorld.spawnLocation.add(Vector(0.0, 35.0, 0.0))
        player.gameMode = GameMode.SPECTATOR
        player.teleport(spectatorSpawnCoords)

        val teamStocks = teamsStocks.entries.find { it.key.contains(player) }!!
        teamsStocks[teamStocks.key] = teamStocks.value - 1

        if (teamStocks.value == 0) {
            checkShouldMinigameEnd()

            teamStocks.key.forEach {
                it.sendMessage(Component.text("You are dead!", NamedTextColor.RED))
            }
        } else {
            plugin.launch {
                repeat(5) {
                    player.sendMessage(Component.text("You died! You will respawn in ${5 - it} seconds"))
                    delay(1.ticks)
                }


                val spawnCoords = minigameData.map.spawnPoints.random()
                val tpLocation = Location(minigameWorld, spawnCoords.x, spawnCoords.y, spawnCoords.z)
                player.teleport(tpLocation)

                playerKits[player]!!.initializeKit()
            }
        }
    }

}