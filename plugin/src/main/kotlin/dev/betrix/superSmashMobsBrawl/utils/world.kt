package dev.betrix.superSmashMobsBrawl.utils

import com.github.michaelbull.result.Err
import com.github.michaelbull.result.Ok
import java.io.IOException
import java.nio.file.*
import java.nio.file.attribute.BasicFileAttributes
import org.bukkit.Bukkit
import org.bukkit.World
import org.bukkit.WorldCreator

object WorldUtils {

    private const val WORLD_PREFIX = "ssmbworld_"

    /** Adds the SSMB world prefix to a world name if not already present */
    private fun addWorldPrefix(worldName: String): String {
        return if (worldName.startsWith(WORLD_PREFIX)) {
            worldName
        } else {
            "$WORLD_PREFIX$worldName"
        }
    }

    /** Removes the SSMB world prefix from a world name if present */
    private fun removeWorldPrefix(worldName: String): String {
        return if (worldName.startsWith(WORLD_PREFIX)) {
            worldName.removePrefix(WORLD_PREFIX)
        } else {
            worldName
        }
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

    /**
     * Copies a world from ./worlds directory and loads it with a custom name (automatically
     * prefixed)
     *
     * @param sourceWorldName Name of the world folder in ./worlds directory
     * @param customWorldName Custom name for the new world (will be prefixed with "ssmbworld_")
     * @return Result containing the loaded World or error
     */
    fun copyAndLoadWorld(
        sourceWorldName: String,
        customWorldName: String,
    ): com.github.michaelbull.result.Result<World, Exception> {
        try {
            val serverFolder = Bukkit.getWorldContainer().toPath()
            val worldsFolder = serverFolder.resolve("worlds")
            val sourceWorldPath = worldsFolder.resolve(sourceWorldName)

            require(Files.exists(sourceWorldPath)) {
                "Source world '$sourceWorldName' does not exist in ./worlds directory!"
            }

            require(Files.isDirectory(sourceWorldPath)) {
                "Source '$sourceWorldName' is not a directory!"
            }

            // Add prefix to custom world name
            val prefixedWorldName = addWorldPrefix(customWorldName)

            // Check if custom world name already exists
            val targetWorldPath = serverFolder.resolve(prefixedWorldName)
            require(!Files.exists(targetWorldPath) && Bukkit.getWorld(prefixedWorldName) == null) {
                "World with name '$prefixedWorldName' already exists!"
            }

            // Copy the world folder
            copyWorldFolder(sourceWorldPath, targetWorldPath).getOrThrow()

            // Load the world with prefixed name
            val worldCreator = WorldCreator(prefixedWorldName)
            val world =
                Bukkit.createWorld(worldCreator)
                    ?: error("Failed to create world: $prefixedWorldName")

            return Ok(world)
        } catch (e: Exception) {
            return Err(e)
        }
    }

    /**
     * Copies a world from ./worlds directory and loads it with a custom name (async, automatically
     * prefixed)
     *
     * @param sourceWorldName Name of the world folder in ./worlds directory
     * @param customWorldName Custom name for the new world (will be prefixed with "ssmbworld_")
     * @param callback Callback with Result<World>
     */
    fun copyAndLoadWorldAsync(
        sourceWorldName: String,
        customWorldName: String,
        callback: (Result<World>) -> Unit,
    ) {
        Bukkit.getScheduler()
            .runTaskAsynchronously(
                Bukkit.getPluginManager().plugins.first(),
                Runnable {
                    val copyResult = runCatching {
                        val serverFolder = Bukkit.getWorldContainer().toPath()
                        val worldsFolder = serverFolder.resolve("worlds")
                        val sourceWorldPath = worldsFolder.resolve(sourceWorldName)

                        require(Files.exists(sourceWorldPath)) {
                            "Source world '$sourceWorldName' does not exist in ./worlds directory!"
                        }

                        require(Files.isDirectory(sourceWorldPath)) {
                            "Source '$sourceWorldName' is not a directory!"
                        }

                        // Add prefix to custom world name
                        val prefixedWorldName = addWorldPrefix(customWorldName)

                        val targetWorldPath = serverFolder.resolve(prefixedWorldName)
                        require(
                            !Files.exists(targetWorldPath) &&
                                Bukkit.getWorld(prefixedWorldName) == null
                        ) {
                            "World with name '$prefixedWorldName' already exists!"
                        }

                        // Copy the world folder
                        copyWorldFolder(sourceWorldPath, targetWorldPath).getOrThrow()
                        prefixedWorldName
                    }

                    // Load world on main thread
                    Bukkit.getScheduler()
                        .runTask(
                            Bukkit.getPluginManager().plugins.first(),
                            Runnable {
                                val loadResult =
                                    copyResult.fold(
                                        onSuccess = { worldName ->
                                            runCatching {
                                                val worldCreator = WorldCreator(worldName)
                                                Bukkit.createWorld(worldCreator)
                                                    ?: error("Failed to create world: $worldName")
                                            }
                                        },
                                        onFailure = { Result.failure<World>(it) },
                                    )
                                callback(loadResult)
                            },
                        )
                },
            )
    }

    /**
     * Fully deletes a world (unloads and removes all files) Handles both prefixed and non-prefixed
     * world names
     *
     * @param worldName Name of the world to delete (with or without prefix)
     * @return Result indicating success or failure
     */
    fun deleteWorld(worldName: String): Result<Unit> {
        return runCatching {
            val actualWorldName =
                getActualWorldName(worldName)
                    ?: error(
                        "World '$worldName' not found (checked both prefixed and non-prefixed versions)"
                    )

            val serverFolder = Bukkit.getWorldContainer().toPath()
            val worldPath = serverFolder.resolve(actualWorldName)

            // Unload world if it's currently loaded
            val world = Bukkit.getWorld(actualWorldName)
            if (world != null) {
                // Move all players out of the world
                val mainWorld =
                    Bukkit.getWorlds().firstOrNull { it != world }
                        ?: error("No other world available to move players to!")

                world.players.forEach { player -> player.teleport(mainWorld.spawnLocation) }

                // Unload the world
                require(Bukkit.unloadWorld(world, false)) {
                    "Failed to unload world: $actualWorldName"
                }
            }

            // Delete world files
            if (Files.exists(worldPath)) {
                deleteWorldFolder(worldPath).getOrThrow()
            }
        }
    }

    /**
     * Fully deletes a world (unloads and removes all files)
     *
     * @param world The World object to delete
     * @return Result indicating success or failure
     */
    fun deleteWorld(world: World): Result<Unit> {
        return deleteWorld(world.name)
    }

    /**
     * Fully deletes a world asynchronously Handles both prefixed and non-prefixed world names
     *
     * @param worldName Name of the world to delete (with or without prefix)
     * @param callback Callback with Result<Unit>
     */
    fun deleteWorldAsync(worldName: String, callback: (Result<Unit>) -> Unit) {
        // Unload world on main thread first
        val unloadResult = runCatching {
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

        if (unloadResult.isFailure) {
            callback(Result.failure(unloadResult.exceptionOrNull()!!))
            return
        }

        // Delete files asynchronously
        Bukkit.getScheduler()
            .runTaskAsynchronously(
                Bukkit.getPluginManager().plugins.first(),
                Runnable {
                    val deleteResult = runCatching {
                        val serverFolder = Bukkit.getWorldContainer().toPath()
                        val worldPath = serverFolder.resolve(unloadResult.getOrNull()!!)

                        if (Files.exists(worldPath)) {
                            deleteWorldFolder(worldPath).getOrThrow()
                        }
                    }

                    // Return result on main thread
                    Bukkit.getScheduler()
                        .runTask(
                            Bukkit.getPluginManager().plugins.first(),
                            Runnable { callback(deleteResult) },
                        )
                },
            )
    }

    /**
     * Fully deletes a world asynchronously
     *
     * @param world The World object to delete
     * @param callback Callback with Result<Unit>
     */
    fun deleteWorldAsync(world: World, callback: (Result<Unit>) -> Unit = {}) {
        deleteWorldAsync(world.name, callback)
    }

    /**
     * Lists all available world templates in ./worlds directory
     *
     * @return List of world template names
     */
    fun listWorldTemplates(): Result<List<String>> {
        return runCatching {
            val serverFolder = Bukkit.getWorldContainer().toPath()
            val worldsFolder = serverFolder.resolve("worlds")

            if (!Files.exists(worldsFolder)) {
                Files.createDirectories(worldsFolder)
                return@runCatching emptyList<String>()
            }

            Files.list(worldsFolder)
                .filter { Files.isDirectory(it) }
                .map { it.fileName.toString() }
                .toList()
        }
    }

    /**
     * Checks if a world name is available (not loaded and no folder exists) Checks both the base
     * name and prefixed version
     *
     * @param worldName Name to check (will check with prefix added)
     * @return true if available, false if taken
     */
    fun isWorldNameAvailable(worldName: String): Boolean {
        val prefixedName = addWorldPrefix(worldName)
        val serverFolder = Bukkit.getWorldContainer().toPath()
        val worldPath = serverFolder.resolve(prefixedName)
        return !Files.exists(worldPath) && Bukkit.getWorld(prefixedName) == null
    }

    // Private helper functions

    private fun copyWorldFolder(source: Path, target: Path): Result<Unit> {
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

                        runCatching { Files.createDirectories(targetDir) }
                            .onFailure { exception ->
                                if (!Files.exists(targetDir)) {
                                    throw exception
                                }
                            }

                        return FileVisitResult.CONTINUE
                    }

                    override fun visitFile(
                        file: Path,
                        attrs: BasicFileAttributes,
                    ): FileVisitResult {
                        val fileName = file.fileName.toString()

                        // Skip problematic files
                        if (fileName == "session.lock" || fileName == "uid.dat") {
                            return FileVisitResult.CONTINUE
                        }

                        val targetFile = target.resolve(source.relativize(file))
                        Files.copy(file, targetFile, StandardCopyOption.REPLACE_EXISTING)

                        return FileVisitResult.CONTINUE
                    }
                },
            )
        }
    }

    private fun deleteWorldFolder(worldPath: Path): Result<Unit> {
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
}
