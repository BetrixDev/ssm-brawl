package dev.betrix.supersmashmobsbrawl.minigames

import dev.betrix.supersmashmobsbrawl.kits.CreeperKit
import dev.betrix.supersmashmobsbrawl.kits.SSMBKit
import dev.betrix.supersmashmobsbrawl.managers.api.payloads.StartGameResponse
import dev.betrix.supersmashmobsbrawl.maps.GameMap
import net.kyori.adventure.audience.Audience
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.entity.PlayerDeathEvent
import java.util.*

class SinglePlayerTestingGame(private val gameData: StartGameResponse) : SSMBGame() {

    private val map = GameMap(gameData.gameId, gameData.map.mapId)
    private val generalAudience: Audience
    private val players = hashMapOf<Player, SSMBKit>()

    init {
        map.createWorld()

        gameData.players.forEachIndexed { playerIndex, data ->
            val player = Bukkit.getPlayer(UUID.fromString(data.uuid))!!

            if (data.kit.id == "creeper") {
                players[player] = CreeperKit(player, data.kit)
            }

            val spawnPos = gameData.map.spawnLocations[playerIndex]
            map.teleportPlayer(
                player,
                Location(map.worldInstance, spawnPos.x.toDouble(), spawnPos.y.toDouble(), spawnPos.z.toDouble())
            )
        }

        generalAudience = Audience.audience(players.keys)
    }

    @EventHandler
    fun onPlayerDeath(event: PlayerDeathEvent) {
        val target = event.player

        if (!players.contains(target)) {
            return
        }

        // TODO: HANDLE PLAYER DEATH
    }
}