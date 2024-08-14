package net.ssmb.services

import com.destroystokyo.paper.utils.PaperPluginLogger
import java.io.File
import net.ssmb.blockwork.Blockwork
import net.ssmb.blockwork.addTag
import net.ssmb.blockwork.annotations.Service
import net.ssmb.components.worlds.HubWorldComponent
import net.ssmb.lifecycles.OnPluginDisable
import net.ssmb.lifecycles.OnServerLoad
import org.bukkit.Bukkit
import org.bukkit.World
import org.bukkit.WorldCreator

@Service
class WorldService(private val logger: PaperPluginLogger) : OnPluginDisable, OnServerLoad {
    private val loadedWorlds = arrayListOf<World>()

    override fun onServerLoad() {
        logger.info("Attempting to create hub world")

        val existingHubComponents = Blockwork.components.getAllWorldComponents<HubWorldComponent>()

        if (existingHubComponents.isEmpty()) {
            logger.info("No hub world component found, creating a new one.")

            val baseWorldDirectory = File("ssmb_worlds/blue_forest")
            val copyWorldDirectory = File("servers_hub")

            if (copyWorldDirectory.exists()) copyWorldDirectory.deleteRecursively()
            baseWorldDirectory.copyRecursively(copyWorldDirectory, true)

            val uidPath = File("${copyWorldDirectory.path}/uid.dat")
            val sessionPath = File("${copyWorldDirectory.path}/session.dat")

            if (uidPath.exists()) uidPath.delete()
            if (sessionPath.exists()) sessionPath.delete()

            val worldCreator = WorldCreator(copyWorldDirectory.path)
            println("world name ${worldCreator.name()}")

            val hubWorld =
                worldCreator.createWorld() ?: throw Exception("Failed to create hub world")

            hubWorld.addTag("hub")
        } else {
            logger.info("Hub world has already been created")
        }
    }

    override fun onPluginDisable() {
        loadedWorlds.forEach { world ->
            world.players.forEach { player ->
                player.teleport(Bukkit.getServer().worlds[0].spawnLocation)
            }

            Bukkit.getServer().unloadWorld(world, false)
        }
    }
}
