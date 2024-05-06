package net.ssmb.minigames

import com.github.shynixn.mccoroutine.bukkit.launch
import com.github.shynixn.mccoroutine.bukkit.registerSuspendingEvents
import com.github.shynixn.mccoroutine.bukkit.ticks
import io.papermc.paper.entity.LookAnchor
import kotlinx.coroutines.delay
import net.kyori.adventure.text.Component
import net.ssmb.SSMB
import net.ssmb.dtos.minigame.MinigameStartSuccess
import net.ssmb.kits.IKit
import net.ssmb.kits.constructKitFromData
import org.bukkit.GameMode
import org.bukkit.Location
import org.bukkit.World
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.entity.PlayerDeathEvent
import org.bukkit.util.Vector

class TestMinigame(
    override val teams: List<List<Player>>,
    private val minigameData: MinigameStartSuccess
) : IMinigame, Listener {
    private val players = teams.flatten()
    private val playerMinigameData = minigameData.teams.flatten()
    private val plugin = SSMB.instance
    private lateinit var minigameWorld: World
    override val playerKits = hashMapOf<Player, IKit>()
    override val teamsStocks = arrayListOf<Pair<ArrayList<Player>, Int>>()

    init {
        plugin.server.pluginManager.registerSuspendingEvents(this, plugin)
    }

    override suspend fun initializeMinigame() {
        minigameWorld = plugin.worlds.createSsmbWorld(minigameData.map.id, minigameData.gameId)

        teams.forEachIndexed { idx, team ->
            val spawnCoords = minigameData.map.spawnPoints[idx]
            val tpLocation = Location(minigameWorld, spawnCoords.x, spawnCoords.y, spawnCoords.z)

            team.forEach { plr ->
                plr.teleport(tpLocation)
                plr.walkSpeed = 0.0f
                plr.lookAt(minigameWorld.spawnLocation, LookAnchor.EYES)

                val playerData =
                    playerMinigameData.find { it -> it.uuid == plr.uniqueId.toString() }!!
                val kit = constructKitFromData(plr, playerData.selectedKit, this)
                kit.initializeKit()

                playerKits[plr] = kit
            }

            teamsStocks.add(Pair(ArrayList(team), minigameData.minigame.stocks))
        }

        minigameWorld.sendMessage(Component.text("Testing game has started!"))
    }

    override fun removePlayer(player: Player) {
        TODO("Not yet implemented")
    }

    @EventHandler
    suspend fun onPlayerDeath(event: PlayerDeathEvent) {
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

            playerKits[player]!!.initializeKit()
        }
    }
}
