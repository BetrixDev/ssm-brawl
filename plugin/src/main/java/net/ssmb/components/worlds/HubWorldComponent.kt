package net.ssmb.components.worlds

import io.papermc.paper.entity.LookAnchor
import net.ssmb.blockwork.annotations.Component
import net.ssmb.blockwork.components.WorldComponent
import net.ssmb.lifecycles.OnPlayerJoined
import org.bukkit.World
import org.bukkit.entity.Player

@Component
class HubWorldComponent : WorldComponent(), OnPlayerJoined {
    companion object Meta {
        fun predicate(world: World): Boolean {
            return world.name.contains("hub")
        }
    }

    override fun onPlayerJoined(player: Player) {
        val hubSpawnLocation = world.spawnLocation
        hubSpawnLocation.x = -29.5
        hubSpawnLocation.y = 58.0
        hubSpawnLocation.z = 1.5

        player.teleport(hubSpawnLocation)
        player.lookAt(-57.5, 59.0, 1.0, LookAnchor.EYES)
    }
}
