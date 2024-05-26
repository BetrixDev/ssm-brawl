package net.ssmb.minigames

import com.github.shynixn.mccoroutine.bukkit.launch
import com.github.shynixn.mccoroutine.bukkit.registerSuspendingEvents
import com.github.shynixn.mccoroutine.bukkit.ticks
import io.papermc.paper.entity.LookAnchor
import kotlinx.coroutines.delay
import net.kyori.adventure.text.Component
import net.ssmb.SSMB
import net.ssmb.dtos.minigame.BukkitTeamData
import net.ssmb.dtos.minigame.MinigameStartSuccess
import net.ssmb.kits.SsmbKit
import net.ssmb.kits.constructKitFromData
import org.bukkit.GameMode
import org.bukkit.Location
import org.bukkit.World
import org.bukkit.attribute.Attribute
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.entity.PlayerDeathEvent
import org.bukkit.event.player.PlayerQuitEvent
import org.bukkit.util.Vector

class TestMinigame(
    override val teams: List<BukkitTeamData>,
    private val minigameData: MinigameStartSuccess
) : IMinigame, Listener {
    private val players = teams.map { it.players }.flatten()
    private val playersLeftInProgress = arrayListOf<Player>()
    private val playerMinigameData = minigameData.teams.map { it.players }.flatten()
    private val plugin = SSMB.instance
    private lateinit var minigameWorld: World
    override val playerKits = hashMapOf<Player, SsmbKit>()
    override val teamsStocks = hashMapOf<String, Int>()
    private var isMinigameRunning = true

    init {
        plugin.server.pluginManager.registerSuspendingEvents(this, plugin)
    }

    override suspend fun initializeMinigame() {
        minigameWorld = plugin.worlds.createSsmbWorld(minigameData.map.id, minigameData.gameId)

        teams.forEachIndexed { idx, team ->
            val spawnCoords = minigameData.map.spawnPoints[idx]
            val tpLocation = Location(minigameWorld, spawnCoords.x, spawnCoords.y, spawnCoords.z)

            team.players.forEach { plr ->
                plr.foodLevel = 20
                plr.health = plr.getAttribute(Attribute.GENERIC_MAX_HEALTH)?.value ?: 20.0
                plr.teleport(tpLocation)
                plr.lookAt(minigameWorld.spawnLocation, LookAnchor.EYES)

                val playerData =
                    playerMinigameData.find { it -> it.uuid == plr.uniqueId.toString() }!!
                val kit = constructKitFromData(plr, playerData.selectedKit, this)
                kit.initializeKit()

                playerKits[plr] = kit
            }

            teamsStocks[team.teamId] = minigameData.minigame.stocks
        }

        minigameWorld.sendMessage(Component.text("Testing game has started!"))

        plugin.launch {
            while (isMinigameRunning) {
                players.forEach { plr ->
                    if (plr.location.y <= minigameData.map.voidYLevel) {
                        val deathEvent =
                            PlayerDeathEvent(plr, emptyList(), 0, "Fell out of the world")
                        deathEvent.callEvent()
                    }
                }

                delay(1.ticks)
            }
        }
    }

    override fun removePlayer(player: Player) {
        TODO("Not yet implemented")
    }

    @EventHandler
    fun onPlayerDeath(event: PlayerDeathEvent) {
        val player = event.entity

        if (!players.contains(player)) return

        event.isCancelled = true

        playerKits[player]!!.destroyKit()

        val spectatorSpawnCoords = minigameWorld.spawnLocation.add(Vector(0.0, 50.0, 0.0))
        player.gameMode = GameMode.SPECTATOR
        player.teleport(spectatorSpawnCoords)

        plugin.launch {
            repeat(5) {
                player.sendMessage(
                    Component.text("You died! You will respawn in ${5 - it} seconds")
                )
                delay(20.ticks)
            }

            val spawnCoords = minigameData.map.spawnPoints.random()
            val tpLocation = Location(minigameWorld, spawnCoords.x, spawnCoords.y, spawnCoords.z)
            player.teleport(tpLocation)
            player.gameMode = GameMode.SURVIVAL

            playerKits[player]!!.initializeKit()
        }
    }

    @EventHandler
    fun onPlayerQuit(event: PlayerQuitEvent) {
        val player = event.player

        if (!players.contains(player)) return

        playerKits[player]!!.destroyKit()
        playersLeftInProgress.add(player)

        if (playersLeftInProgress.size == players.size) {
            println("All players have left the game (${minigameData.gameId})! Cleaning up...")

            playerKits.values.forEach { it.destroyKit() }

            plugin.worlds.deleteSsmbWorld(minigameData.gameId)
        }
    }
}
