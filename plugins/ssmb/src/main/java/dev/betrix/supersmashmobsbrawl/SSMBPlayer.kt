package dev.betrix.supersmashmobsbrawl

import dev.betrix.supersmashmobsbrawl.managers.api.payloads.StartGameResponse
import org.bukkit.entity.Player
import java.util.*

class SSMBPlayer(val bukkitPlayer: Player) {

    var selectedKitData: StartGameResponse.KitData? = null
    var abilities = listOf<StartGameResponse.AbilitiesData>()
    var passives = listOf<StartGameResponse.PassivesData>()
    val cooldowns = hashMapOf<String, Long>()

    companion object {
        private val players = hashMapOf<UUID, SSMBPlayer>()

        fun addPlayer(player: Player) {
            players[player.uniqueId] = SSMBPlayer(player)
        }

        fun fromUuid(uuid: UUID): SSMBPlayer? {
            return players[uuid]
        }
    }
}