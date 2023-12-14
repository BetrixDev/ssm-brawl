package dev.betrix.supersmashmobsbrawl.minigames

import br.com.devsrsouza.kotlinbukkitapi.extensions.item
import dev.betrix.supersmashmobsbrawl.SSMBPlayer
import dev.betrix.supersmashmobsbrawl.enums.TaggedKeyNum
import dev.betrix.supersmashmobsbrawl.enums.TaggedKeyStr
import dev.betrix.supersmashmobsbrawl.extensions.setData
import dev.betrix.supersmashmobsbrawl.managers.api.payloads.StartGameResponse
import dev.betrix.supersmashmobsbrawl.maps.GameMap
import net.kyori.adventure.audience.Audience
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.entity.PlayerDeathEvent
import org.bukkit.inventory.EquipmentSlot
import java.util.*

class SinglePlayerTestingGame(private val gameData: StartGameResponse) : Listener {

    private val map = GameMap(gameData.gameId, gameData.map.mapId)
    private val generalAudience: Audience
    private val players: ArrayList<Player> = arrayListOf()

    init {
        gameData.players.forEachIndexed { playerIndex, data ->
            val player = Bukkit.getPlayer(UUID.fromString(data.uuid))!!
            val ssmbPlayer = SSMBPlayer.fromUuid(player.uniqueId)!!

            ssmbPlayer.abilities = data.kit.abilities
            ssmbPlayer.passives = data.kit.passives
            ssmbPlayer.selectedKitData = data.kit

            data.kit.visualArmor.forEach {
                player.inventory.setItem(
                    EquipmentSlot.valueOf(it.slot.uppercase()),
                    item(Material.getMaterial(it.id.uppercase())!!)
                )
            }

            data.kit.abilities.forEachIndexed { i, abilityData ->
                val tool = item(Material.getMaterial(abilityData.toolId.uppercase())!!, meta = {
                    persistentDataContainer.setData {
                        set(TaggedKeyNum.TOOL_MELEE_DAMAGE, data.kit.damage)
                        set(TaggedKeyStr.ABILITY_ITEM_ID, abilityData.id)
                    }
                })

                player.inventory.setItem(i, tool)
            }

            val spawnPos = gameData.map.spawnLocations[playerIndex]
            map.teleportPlayer(
                player,
                Location(map.worldInstance, spawnPos.x.toDouble(), spawnPos.y.toDouble(), spawnPos.z.toDouble())
            )

            player.allowFlight = true

            players.add(player)
        }

        generalAudience = Audience.audience(players)
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