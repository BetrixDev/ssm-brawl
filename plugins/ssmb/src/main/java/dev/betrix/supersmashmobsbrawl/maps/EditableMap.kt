package dev.betrix.supersmashmobsbrawl.maps

import dev.betrix.supersmashmobsbrawl.enums.WorldGeneratorType
import org.bukkit.entity.Player

class EditableMap(
    serverId: String,
    worldId: String, private val players: ArrayList<Player>
) : SSMBMap(serverId, worldId, WorldGeneratorType.VOID) {
    override fun afterWorldLoad() {
        players.forEach {
            teleportPlayer(it)
        }
    }
}