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
import org.bukkit.Difficulty
import org.bukkit.Location
import org.bukkit.World
import org.bukkit.WorldType
import org.bukkit.entity.Player
import java.io.File

abstract class SSMBMap constructor(
    private val serverName: String,
    private val worldId: String,
    private val generatorType: WorldGeneratorType
) {
    private val worldName = "ssmb_world_$serverName"
    private lateinit var world: MultiverseWorld
    lateinit var worldInstance: World
    lateinit var schematicManager: SchematicManager

    companion object {
        val cwd: String = System.getProperty("user.dir")
        val json = Json { ignoreUnknownKeys = true }
        private val plugin = SuperSmashMobsBrawl.instance

        private val loadedWorlds = arrayListOf<SSMBMap>()

        fun clearLoadedWorlds() {
            loadedWorlds.forEach {
                it.destroyWorld()
            }
        }
    }

    open fun createWorld() {
        val worldMetadata = readMetadata()

        val schematicOrigin = Vector3.at(
            worldMetadata.schematicLocation.x,
            worldMetadata.schematicLocation.y,
            worldMetadata.schematicLocation.z
        )

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
            worldInstance.worldBorder.size = worldMetadata.worldBorderRadius * 2
            worldInstance.spawnLocation = Location(
                worldInstance,
                worldMetadata.spawnLocation.x,
                worldMetadata.spawnLocation.y,
                worldMetadata.spawnLocation.z
            )
            world.adjustSpawn = true
            world.autoHeal = true
            world.difficulty = Difficulty.PEACEFUL
            world.setPVPMode(false)
            world.setAllowAnimalSpawn(false)
            world.setAllowMonsterSpawn(false)
            world.spawnLocation = Location(
                worldInstance,
                worldMetadata.spawnLocation.x,
                worldMetadata.spawnLocation.y,
                worldMetadata.spawnLocation.z
            )

            loadedWorlds.add(this)
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

    fun readMetadata(): WorldMetadata {
        val metaJson = File("$cwd//worlds//$worldId//meta.json")

        return json.decodeFromString(metaJson.readText())
    }

    open fun destroyWorld() {
        loadedWorlds.remove(this)
        plugin.mvc.deleteWorld(worldName)
    }

    fun teleportPlayer(player: Player, location: Location? = null) {
        plugin.mvc.teleportPlayer(player, player, location ?: worldInstance.spawnLocation)

        afterPlayerTeleport(player)
    }

    open fun afterPlayerTeleport(player: Player) {}
}

@Serializable
data class WorldMetadata(
    val worldBorderRadius: Double,
    val schematicRadius: Double,
    val schematicLowerLimit: Double,
    val schematicUpperLimit: Double,
    val spawnLocation: SpawnLocation,
    val schematicLocation: SpawnLocation,
)

@Serializable
data class SpawnLocation(
    val x: Double,
    val y: Double,
    val z: Double
)