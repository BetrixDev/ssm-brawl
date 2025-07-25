package dev.betrix.superSmashMobsBrawl.maps

import org.bukkit.util.Vector

enum class MapType {
    MINIGAME,
    HUB
}

data class SsmbMap(
    val name: String,
    val id: String,
    val type: MapType,
    val maxPlayers: Int?,
    val description: String,
    val voidLevel: Int,
    val creatorUuids: List<String>,
    val spawnPoints: List<Vector>,
    val isDefault: Boolean = false
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
    private val spawnPoints = mutableListOf<Vector>()

    fun addCreator(creatorUuid: String) {
        creatorUuids.add(creatorUuid)
    }

    fun addSpawnPoint(vector: Vector) {
        spawnPoints.add(vector)
    }

    fun addSpawnPoint(x: Double, y: Double, z: Double) {
        spawnPoints.add(Vector(x, y, z))
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
            isDefault = isDefault
        )
    }
}

class SpawnPointsBuilder {
    internal val points = mutableListOf<Vector>()

    fun at(x: Double, y: Double, z: Double) {
        points.add(Vector(x, y, z))
    }

    fun at(vector: Vector) {
        points.add(vector)
    }
}

fun defineMap(block: MapBuilder.() -> Unit): SsmbMap {
    val builder = MapBuilder()
    builder.block()
    return builder.build()
}