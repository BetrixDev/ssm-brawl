package dev.betrix.superSmashMobsBrawl.maps

import org.bukkit.util.Vector

enum class MapType {
    MINIGAME,
    HUB,
}

data class SpawnPoint(val position: Vector, val yaw: Float? = null, val pitch: Float? = null)

data class SsmbMap(
    val name: String,
    val id: String,
    val type: MapType,
    val maxPlayers: Int?,
    val description: String,
    val voidLevel: Int,
    val creatorUuids: List<String>,
    val spawnPoints: List<SpawnPoint>,
)

class MapBuilder {
    var name = ""
    var id = ""
    var maxPlayers: Int? = null
    var description = ""
    var type = MapType.MINIGAME
    var voidLevel = 0
    var isDefault = false
    var creatorUuids = hashSetOf<String>()
    private val spawnPoints = mutableListOf<SpawnPoint>()

    fun addCreator(creatorUuid: String) {
        creatorUuids.add(creatorUuid)
    }

    fun addSpawnPoint(vector: Vector, yaw: Float? = null, pitch: Float? = null) {
        spawnPoints.add(SpawnPoint(vector, yaw, pitch))
    }

    fun addSpawnPoint(x: Double, y: Double, z: Double, yaw: Float? = null, pitch: Float? = null) {
        spawnPoints.add(SpawnPoint(Vector(x, y, z), yaw, pitch))
    }

    fun spawnPoints(block: SpawnPointsBuilder.() -> Unit) {
        val spawnBuilder = SpawnPointsBuilder()
        spawnBuilder.block()
        spawnPoints.addAll(spawnBuilder.points)
    }

    internal fun build(): SsmbMap {
        require(name.isNotEmpty()) { "Map name cannot be empty" }
        require(id.isNotEmpty()) { "Map id cannot be empty" }

        return SsmbMap(
            name = name,
            id = id,
            type = type,
            spawnPoints = spawnPoints.toList(),
            maxPlayers = maxPlayers,
            description = description,
            voidLevel = voidLevel,
            creatorUuids = creatorUuids.toList(),
        )
    }
}

class SpawnPointsBuilder {
    internal val points = mutableListOf<SpawnPoint>()

    fun at(x: Double, y: Double, z: Double, yaw: Float? = null, pitch: Float? = null) {
        points.add(SpawnPoint(Vector(x, y, z), yaw, pitch))
    }

    fun at(vector: Vector, yaw: Float? = null, pitch: Float? = null) {
        points.add(SpawnPoint(vector, yaw, pitch))
    }
}

fun defineMap(block: MapBuilder.() -> Unit): SsmbMap {
    val builder = MapBuilder()
    builder.block()
    return builder.build()
}
