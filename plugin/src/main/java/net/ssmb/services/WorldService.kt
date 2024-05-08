package net.ssmb.services

import java.io.File
import net.ssmb.SSMB
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.World
import org.bukkit.WorldCreator
import org.codehaus.plexus.util.FileUtils

class WorldService {
    private val plugin = SSMB.instance
    private val loadedWorlds = hashMapOf<String, World>()

    suspend fun createSsmbWorld(mapId: String, serverName: String): World {
        val mapDetails = plugin.api.mapGetMapDetails(mapId)

        val baseWorldDirectory = File("ssmb_worlds/$mapId")
        val copyDirectory = File("servers_$serverName")

        if (copyDirectory.exists()) {
            throw RuntimeException("Server already exists with name $serverName")
        }

        try {
            baseWorldDirectory.copyRecursively(copyDirectory, true)
            //            FileUtils.copyDirectory(baseWorldDirectory, copyDirectory, "**/*", null)
            val uidPath = File("${copyDirectory.path}/uid.dat")
            val sessionPath = File("${copyDirectory.path}/session.dat")

            if (uidPath.exists()) {
                uidPath.delete()
            }
            if (sessionPath.exists()) {
                sessionPath.delete()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        val worldCreator = WorldCreator(copyDirectory.path)
        val world = worldCreator.createWorld() ?: throw Exception("Error loading copied world")
        world.isAutoSave = false

        world.setSpawnLocation(
            Location(world, mapDetails.origin.x, mapDetails.origin.y, mapDetails.origin.z)
        )

        loadedWorlds[serverName] = world

        return world
    }

    fun deleteSsmbWorld(serverName: String) {
        val loadedWorld = loadedWorlds[serverName] ?: return

        Bukkit.unloadWorld(loadedWorld, false)

        try {
            FileUtils.deleteDirectory("servers_$serverName")
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun deleteAllLoadedWorlds() {
        val mainWorld = Bukkit.getWorlds()[0]

        loadedWorlds.forEach { (k, v) ->
            v.players.forEach { it.teleport(mainWorld.spawnLocation) }

            Bukkit.unloadWorld(v, false)

            try {
                FileUtils.deleteDirectory("servers/$k")
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}
