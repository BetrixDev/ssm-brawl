package dev.betrix.supersmashmobsbrawl.minigames

import br.com.devsrsouza.kotlinbukkitapi.extensions.item
import dev.betrix.supersmashmobsbrawl.SSMBPlayer
import dev.betrix.supersmashmobsbrawl.SuperSmashMobsBrawl
import dev.betrix.supersmashmobsbrawl.enums.GameState
import dev.betrix.supersmashmobsbrawl.enums.TaggedKey
import dev.betrix.supersmashmobsbrawl.extensions.setDouble
import dev.betrix.supersmashmobsbrawl.extensions.setString
import dev.betrix.supersmashmobsbrawl.extensions.setValues
import dev.betrix.supersmashmobsbrawl.kits.BaseKit
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
import org.bukkit.persistence.PersistentDataType
import java.util.*

class SinglePlayerTestingGame(private val gameData: StartGameResponse) : Listener {

    private val plugin = SuperSmashMobsBrawl.instance
    private val map = GameMap(gameData.gameId, gameData.map.mapId)
    private val gameState = GameState.COUNTDOWN
    private val generalAudience: Audience
    private val players: ArrayList<Player> = arrayListOf()
    private val kits: HashMap<Player, BaseKit> = hashMapOf()

    init {
        gameData.players.forEachIndexed { playerIndex, data ->
            val player = Bukkit.getPlayer(UUID.fromString(data.uuid))!!
            val ssmbPlayer = SSMBPlayer.fromUuid(player.uniqueId)!!

            ssmbPlayer.abilities = data.kit.abilities
            ssmbPlayer.passives = data.kit.passives
            ssmbPlayer.selectedKitData = data.kit

            data.kit.visualArmor.forEach {
                player.inventory.setItem(EquipmentSlot.valueOf(it.slot), item(Material.getMaterial(it.id)!!))
            }

            data.kit.abilities.forEachIndexed { i, abilityData ->
                val tool = item(Material.getMaterial(abilityData.toolId)!!)
                val newMetaData = tool.itemMeta
                newMetaData.persistentDataContainer.setValues {
                    setDouble(TaggedKey.TOOL_MELEE_DAMAGE, data.kit.damage)

                    if (abilityData.abilityId != null) {
                        setString(TaggedKey.ABILITY_ITEM_ID, abilityData.abilityId)
                        plugin.logger.info(get(TaggedKey.ABILITY_ITEM_ID.key, PersistentDataType.STRING))
                    }
                }
                tool.itemMeta = newMetaData
                player.inventory.setItem(i, tool)
            }

            val spawnPos = gameData.map.spawnLocations[playerIndex]
            map.teleportPlayer(
                player,
                Location(map.worldInstance, spawnPos.x.toDouble(), spawnPos.y.toDouble(), spawnPos.z.toDouble())
            )

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