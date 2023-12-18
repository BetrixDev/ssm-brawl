package dev.betrix.supersmashmobsbrawl.maps

import dev.betrix.supersmashmobsbrawl.SuperSmashMobsBrawl
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.World
import org.bukkit.WorldCreator
import org.bukkit.entity.Player
import org.codehaus.plexus.util.FileUtils
import java.io.File

open class SSMBMap constructor(
    private val serverName: String,
    private val mapId: String
) {
    private val plugin = SuperSmashMobsBrawl.instance
    val worldInstance: World

    companion object {
        private val cwd = System.getProperty("user.dir")

        fun clearCurrentWorlds() {
            val worldDirectory = File("$cwd\\current_worlds")

            FileUtils.deleteDirectory(worldDirectory)
        }

        fun prepareShutdown() {}
    }

    init {
        val baseWorldDirectory = File("$cwd\\worlds\\$mapId")
        val copiedWorldDirectory = File("$cwd\\current_worlds\\$serverName")

        FileUtils.copyDirectoryStructure(baseWorldDirectory, copiedWorldDirectory)

        if (Bukkit.getWorld(copiedWorldDirectory.path) != null) {
            throw RuntimeException()
        }

        val worldCreator = WorldCreator.name("current_worlds/$serverName")

        worldInstance = plugin.server.createWorld(worldCreator) ?: plugin.server.worlds[0]
        worldInstance.isAutoSave = false
    }

    fun teleportAllToDefaultWorld() {
        val defaultWorld = Bukkit.getWorlds()[0]

        worldInstance.players.forEach {
            it.teleport(defaultWorld.spawnLocation)
        }
    }

    fun teleportPlayer(player: Player, location: Location? = null) {
        player.teleport(location ?: worldInstance.spawnLocation)

        afterPlayerTeleport(player)
    }

    open fun afterPlayerTeleport(player: Player) {}

    fun destroyServer() {}
}