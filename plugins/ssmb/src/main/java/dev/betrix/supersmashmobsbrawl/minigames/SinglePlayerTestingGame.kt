package dev.betrix.supersmashmobsbrawl.minigames

import dev.betrix.supersmashmobsbrawl.SuperSmashMobsBrawl
import dev.betrix.supersmashmobsbrawl.enums.GameState
import dev.betrix.supersmashmobsbrawl.kits.BaseKit
import dev.betrix.supersmashmobsbrawl.managers.api.payloads.StartGameResponse
import dev.betrix.supersmashmobsbrawl.maps.GameMap
import net.kyori.adventure.audience.Audience
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.entity.Player
import java.util.*

class SinglePlayerTestingGame(private val gameData: StartGameResponse) {

    private val plugin = SuperSmashMobsBrawl.instance
    private val map = GameMap(gameData.gameId, gameData.map.mapId)
    private val gameState = GameState.COUNTDOWN
    private val generalAudience: Audience
    private val players: ArrayList<Player> = arrayListOf()
    private val kits: HashMap<Player, BaseKit> = hashMapOf()

    init {
        gameData.players.forEachIndexed { index, it ->
            val player = Bukkit.getPlayer(UUID.fromString(it.uuid))!!

            val playerKit = BaseKit.fromId(it.selectedKit, player)
            plugin.logger.info(playerKit.toString())

            val spawnPos = gameData.map.spawnLocations[index]
            plugin.logger.info(spawnPos.x.toString())
            plugin.logger.info(spawnPos.y.toString())
            plugin.logger.info(spawnPos.z.toString())
            map.teleportPlayer(
                player,
                Location(map.worldInstance, spawnPos.x.toDouble(), spawnPos.y.toDouble(), spawnPos.z.toDouble())
            )

            playerKit.equipKit()

            players.add(player)
            kits[player] = playerKit
        }

        generalAudience = Audience.audience(players)
    }

//    private val map: GameMap
//    private var gameState = GameState.COUNTDOWN
//
//    init {
//        plugin.server.pluginManager.registerEvents(this, plugin)
//
//        map = GameMap(plugin, "lobby-31", world)
//
//        for (player in players) {
//            map.teleportPlayer(player)
//        }
//
//        val kit = SSMBKit.fromId("creeper")!!
//
//        players.forEach { _ ->
//            val s = kit.kitClass
//        }
//    }
//
//    fun endGame() {
//        gameState = GameState.ENDED
//
//        HandlerList.unregisterAll(this)
//    }
//
//    @EventHandler
//    fun onPlayerMove(event: PlayerMoveEvent) {
//        val target = event.player
//
//        if (target.world != map.worldInstance) {
//            return
//        }
//
//        if (gameState != GameState.RUNNING) {
//            event.isCancelled = true
//        }
//    }
}