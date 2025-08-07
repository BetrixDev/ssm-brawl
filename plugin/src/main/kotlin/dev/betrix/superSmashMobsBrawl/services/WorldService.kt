package dev.betrix.superSmashMobsBrawl.services

import com.github.michaelbull.result.Err
import com.github.michaelbull.result.Ok
import com.github.michaelbull.result.Result
import com.github.michaelbull.result.onFailure
import com.github.shynixn.mccoroutine.bukkit.asyncDispatcher
import com.github.shynixn.mccoroutine.bukkit.minecraftDispatcher
import dev.betrix.superSmashMobsBrawl.SuperSmashMobsBrawl
import dev.betrix.superSmashMobsBrawl.models.BrawlGameWorld
import dev.betrix.superSmashMobsBrawl.models.BrawlHubWorld
import dev.betrix.superSmashMobsBrawl.models.BrawlWorld
import dev.betrix.superSmashMobsBrawl.models.brawlData.GameMapDef
import dev.betrix.superSmashMobsBrawl.models.brawlData.HubMapDef
import dev.betrix.superSmashMobsBrawl.models.brawlData.MapDef
import java.io.IOException
import java.nio.file.FileVisitResult
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.SimpleFileVisitor
import java.nio.file.StandardCopyOption
import java.nio.file.attribute.BasicFileAttributes
import java.util.UUID
import kotlinx.coroutines.withContext
import org.bukkit.Bukkit
import org.bukkit.GameRule
import org.bukkit.World
import org.bukkit.WorldCreator

object WorldService {
    private val loadedWorlds = hashMapOf<String, BrawlWorld>()

    private const val WORLD_PREFIX = "ssmbworld_"

    suspend fun teardown() {
        loadedWorlds.forEach { it ->
            deleteWorld(it.value).onFailure { error ->
                SuperSmashMobsBrawl.instance.logger.severe(
                    "Failed to delete world: ${error.message}"
                )
            }
        }
    }

    suspend fun <T : BrawlWorld> copyAndLoadWorld(
        map: MapDef,
        customWorldName: String = UUID.randomUUID().toString(),
    ): Result<T, Exception> =
        withContext(SuperSmashMobsBrawl.instance.asyncDispatcher) {
            val copyResult = runCatching {
                val serverFolder = Bukkit.getWorldContainer().toPath()
                val worldsFolder = serverFolder.resolve("worlds")
                val sourceWorldPath = worldsFolder.resolve(map.id)

                require(Files.exists(sourceWorldPath)) {
                    "Source world '$map.id' does not exist in ./worlds directory!"
                }

                require(Files.isDirectory(sourceWorldPath)) {
                    "Source '$map.id' is not a directory!"
                }

                val prefixedWorldName = addWorldPrefix(customWorldName)

                val targetWorldPath = serverFolder.resolve(prefixedWorldName)
                require(
                    !Files.exists(targetWorldPath) && Bukkit.getWorld(prefixedWorldName) == null
                ) {
                    "World with name '$prefixedWorldName' already exists!"
                }

                // Copy the world folder
                copyWorldFolder(sourceWorldPath, targetWorldPath).getOrThrow()
                prefixedWorldName
            }

            withContext(SuperSmashMobsBrawl.instance.minecraftDispatcher) {
                copyResult.fold(
                    onSuccess = { worldName ->
                        runCatching {
                                val worldCreator = WorldCreator(worldName)
                                val world = Bukkit.createWorld(worldCreator)

                                if (world == null) {
                                    error("Failed to create world: $worldName")
                                }

                                setupWorld(world, map)

                                world
                            }
                            .fold(
                                onSuccess = { world ->
                                    val loadedWorld =
                                        when (map) {
                                            is GameMapDef -> BrawlGameWorld(world, map)
                                            is HubMapDef -> BrawlHubWorld(world, map)
                                        }

                                    loadedWorlds[customWorldName] = loadedWorld

                                    Ok(loadedWorld as T)
                                },
                                onFailure = { Err(RuntimeException(it.message, it)) },
                            )
                    },
                    onFailure = {
                        return@withContext Err(RuntimeException(it.message, it))
                    },
                )
            }
        }

    suspend fun deleteWorld(world: World): Result<Unit, Exception> {
        val loadedWorld = loadedWorlds.entries.find { it.value.world == world }?.value

        if (loadedWorld == null) {
            return Err(Exception("No loaded world found for world $world"))
        }

        return deleteWorld(loadedWorld)
    }

    suspend fun deleteWorld(loadedWorld: BrawlWorld): Result<Unit, Exception> =
        withContext(SuperSmashMobsBrawl.instance.minecraftDispatcher) mainContext@{
            val loadedWorldEntry = loadedWorlds.entries.find { it.value == loadedWorld }

            if (loadedWorldEntry == null) {
                return@mainContext Err(Exception("No loaded world found for $loadedWorld"))
            }

            val worldName = loadedWorldEntry.key

            runCatching {
                    val actualWorldName =
                        getActualWorldName(worldName)
                            ?: error(
                                "World '$worldName' not found (checked both prefixed and non-prefixed versions)"
                            )

                    val world = Bukkit.getWorld(actualWorldName)
                    if (world != null) {
                        val mainWorld =
                            Bukkit.getWorlds().firstOrNull { it != world }
                                ?: error("No other world available to move players to!")

                        world.players.forEach { player -> player.teleport(mainWorld.spawnLocation) }

                        require(Bukkit.unloadWorld(world, false)) {
                            "Failed to unload world: $actualWorldName"
                        }
                    }
                    actualWorldName
                }
                .fold(
                    onSuccess = { actualWorldName ->
                        return@mainContext withContext(
                            SuperSmashMobsBrawl.instance.asyncDispatcher
                        ) {
                            runCatching {
                                    val serverFolder = Bukkit.getWorldContainer().toPath()
                                    val worldPath = serverFolder.resolve(actualWorldName)

                                    if (Files.exists(worldPath)) {
                                        deleteWorldFolder(worldPath).getOrThrow()
                                    }

                                    loadedWorlds.remove(worldName)
                                }
                                .fold(
                                    onSuccess = { Ok(Unit) },
                                    onFailure = { Err(RuntimeException(it.message, it)) },
                                )
                        }
                    },
                    onFailure = { err ->
                        return@mainContext Err(RuntimeException(err.message, err))
                    },
                )
        }

    private fun setupWorld(world: World, map: MapDef) {
        world.worldBorder.size = map.worldBorderSize
        world.setStorm(false)
        world.isVoidDamageEnabled = false
        world.time = 5000L
        world.setGameRule(GameRule.DO_DAYLIGHT_CYCLE, false)
        world.setGameRule(GameRule.DO_WEATHER_CYCLE, false)
        world.setGameRule(GameRule.DO_MOB_SPAWNING, false)
        world.setGameRule(GameRule.ALLOW_FIRE_TICKS_AWAY_FROM_PLAYER, false)
        world.setGameRule(GameRule.DO_MOB_LOOT, false)
        world.setGameRule(GameRule.DO_VINES_SPREAD, false)
        world.setGameRule(GameRule.MOB_GRIEFING, false)
    }

    /**
     * Gets the actual world name that might be prefixed, checking both prefixed and non-prefixed
     * versions
     */
    private fun getActualWorldName(worldName: String): String? {
        val prefixedName = addWorldPrefix(worldName)
        val nonPrefixedName = removeWorldPrefix(worldName)

        return when {
            Bukkit.getWorld(prefixedName) != null -> prefixedName
            Bukkit.getWorld(nonPrefixedName) != null -> nonPrefixedName
            else -> {
                val serverFolder = Bukkit.getWorldContainer().toPath()
                when {
                    Files.exists(serverFolder.resolve(prefixedName)) -> prefixedName
                    Files.exists(serverFolder.resolve(nonPrefixedName)) -> nonPrefixedName
                    else -> null
                }
            }
        }
    }

    private fun deleteWorldFolder(worldPath: Path): kotlin.Result<Unit> {
        return runCatching {
            Files.walkFileTree(
                worldPath,
                object : SimpleFileVisitor<Path>() {
                    override fun visitFile(
                        file: Path,
                        attrs: BasicFileAttributes,
                    ): FileVisitResult {
                        Files.delete(file)
                        return FileVisitResult.CONTINUE
                    }

                    override fun postVisitDirectory(dir: Path, exc: IOException?): FileVisitResult {
                        if (exc != null) throw exc
                        Files.delete(dir)
                        return FileVisitResult.CONTINUE
                    }
                },
            )
        }
    }

    private fun copyWorldFolder(source: Path, target: Path): kotlin.Result<Unit> {
        return runCatching {
            Files.walkFileTree(
                source,
                object : SimpleFileVisitor<Path>() {
                    override fun preVisitDirectory(
                        dir: Path,
                        attrs: BasicFileAttributes,
                    ): FileVisitResult {
                        val targetDir = target.resolve(source.relativize(dir))
                        val dirName = dir.fileName.toString()

                        // Skip problematic directories
                        if (dirName == "session.lock" || dirName == "uid.dat") {
                            return FileVisitResult.SKIP_SUBTREE
                        }

                        Files.createDirectories(targetDir)
                        return FileVisitResult.CONTINUE
                    }

                    override fun visitFile(
                        file: Path,
                        attrs: BasicFileAttributes,
                    ): FileVisitResult {
                        val targetFile = target.resolve(source.relativize(file))
                        Files.createDirectories(targetFile.parent)
                        Files.copy(file, targetFile, StandardCopyOption.REPLACE_EXISTING)
                        return FileVisitResult.CONTINUE
                    }
                },
            )
        }
    }

    private fun addWorldPrefix(worldName: String): String = "$WORLD_PREFIX$worldName"

    private fun removeWorldPrefix(worldName: String): String = worldName.removePrefix(WORLD_PREFIX)
}
