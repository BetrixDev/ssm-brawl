package net.ssmb.services

import net.ssmb.SSMB
import org.bukkit.Bukkit
import org.bukkit.World
import org.bukkit.WorldCreator
import org.codehaus.plexus.util.FileUtils


import java.io.File

class WorldService(private val plugin: SSMB) {
    private val loadedWorlds = hashMapOf<String, World>()
    fun createSsmbWorld(worldName: String, serverName: String): World {
        val baseWorldDirectory = File("ssmb_worlds/$worldName")
        val copyDirectory = File("servers/$serverName")

        if (copyDirectory.exists()) {
            throw RuntimeException("Server already exists with name $serverName")
        }

        try {
            FileUtils.copyDirectory(baseWorldDirectory, copyDirectory)
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

        loadedWorlds[serverName] = world

        return world
    }

    fun deleteSsmbWorld(serverName: String) {
        val loadedWorld = loadedWorlds[serverName] ?: return

        Bukkit.unloadWorld(loadedWorld, false)

        try {
            FileUtils.deleteDirectory("servers/$serverName")
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}