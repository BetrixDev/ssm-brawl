package dev.betrix.supersmashmobsbrawl.maps

import com.github.shynixn.mccoroutine.bukkit.launch
import com.onarandombox.MultiverseCore.api.MultiverseWorld
import com.sk89q.worldedit.math.Vector3
import dev.betrix.supersmashmobsbrawl.SuperSmashMobsBrawl
import dev.betrix.supersmashmobsbrawl.enums.WorldGeneratorType
import dev.betrix.supersmashmobsbrawl.managers.SchematicManager
import kotlinx.coroutines.Dispatchers
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.World
import org.bukkit.WorldType
import org.bukkit.entity.Player
import org.codehaus.plexus.util.FileUtils
import java.io.File

abstract class SSMBMap constructor(
    private val serverName: String,
    private val worldId: String,
    private val generatorType: WorldGeneratorType
) {
    private val plugin = SuperSmashMobsBrawl.instance

    private val worldName = "ssmb_world_$serverName"
    private lateinit var world: MultiverseWorld
    lateinit var worldInstance: World
    lateinit var schematicManager: SchematicManager

    companion object {
        val cwd: String = System.getProperty("user.dir")
        val json = Json { ignoreUnknownKeys = true }

        fun clearCurrentWorlds() {
            val worldDirectory = File("$cwd\\current_worlds")

            FileUtils.deleteDirectory(worldDirectory)
        }
    }

    open fun createWorld() {
        createWorld(Vector3.ZERO)
    }

    open fun createWorld(schematicOrigin: Vector3) {
        val worldGenerator = when (generatorType) {
            WorldGeneratorType.VOID -> "VoidGen"
            WorldGeneratorType.ISLANDS -> "Terra:SKYLANDS"
        }

        val success = plugin.mvc.mvWorldManager.addWorld(
            worldName,
            World.Environment.NORMAL,
            null, WorldType.NORMAL,
            false,
            worldGenerator
        )

        if (success) {
            world = plugin.mvc.mvWorldManager.getMVWorld(worldName)
            worldInstance = world.cbWorld
        } else {
            plugin.logger.info("Unable to create world $worldName / $worldId with generator $generatorType")
        }

        plugin.launch(Dispatchers.IO) {
            schematicManager = SchematicManager(worldInstance, "$cwd\\worlds\\$worldId\\schematic.schem")
            schematicManager.pasteSchematic(
                Location(
                    worldInstance,
                    schematicOrigin.x,
                    schematicOrigin.y,
                    schematicOrigin.z
                )
            )

            if (generatorType !== WorldGeneratorType.VOID) {
                plugin.chunky.startTask(worldId, "square", 0.0, 0.0, 250.0, 250.0, "concentric")
            }
        }
    }

    fun readMetadata(): Metadata {
        val metaJson = File("$cwd//worlds//$worldId//meta.json")

        return json.decodeFromString(metaJson.readText())
    }

    open fun destroyWorld() {
        plugin.mvc.deleteWorld(serverName)
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
}

@Serializable
data class Metadata(
    val worldBorderRadius: Double,
    val schematicRadius: Double,
    val schematicLowerLimit: Double,
    val schematicUpperLimit: Double,
    val spawnLocations: SpawnLocation,
    val schematicLocation: SpawnLocation,
) {
    @Serializable
    data class SpawnLocation(
        val x: Double,
        val y: Double,
        val z: Double
    )

}