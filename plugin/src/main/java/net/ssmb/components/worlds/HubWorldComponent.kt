package net.ssmb.components.worlds

import io.papermc.paper.entity.LookAnchor
import net.ssmb.blockwork.addTag
import net.ssmb.blockwork.annotations.Component
import net.ssmb.blockwork.components.WorldComponent
import net.ssmb.blockwork.interfaces.OnDestroy
import net.ssmb.lifecycles.OnPlayerJoined
import org.bukkit.Bukkit
import org.bukkit.entity.Player

@Component("hub")
class HubWorldComponent : WorldComponent(), OnPlayerJoined, OnDestroy {
    override fun onPlayerJoined(player: Player) {
        val hubSpawnLocation = world.spawnLocation
        hubSpawnLocation.x = -29.5
        hubSpawnLocation.y = 58.0
        hubSpawnLocation.z = 1.5

        player.teleport(hubSpawnLocation)
        player.lookAt(-57.5, 59.0, 1.0, LookAnchor.EYES)
        player.addTag("passive_double_jump")
    }

    override fun onDestroy() {
        val defaultWorld = Bukkit.getServer().worlds.first()

        world.players.forEach { it.teleport(defaultWorld.spawnLocation) }
    }
}
