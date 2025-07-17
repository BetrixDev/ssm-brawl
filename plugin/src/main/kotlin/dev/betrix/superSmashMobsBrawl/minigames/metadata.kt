package dev.betrix.superSmashMobsBrawl.minigames

data class MinigameMetadata(
    val description: String = "",
    val isHidden: Boolean = false,
    val mapWhitelist: Set<String> = setOf(),
    val mapBlackList: Set<String> = setOf()
)

class Minigame {
    var description = ""
    var isHidden = false
    private var mapWhiteList = hashSetOf<String>()
    private var mapBlackList = hashSetOf<String>()

    fun whitelistMap(mapId: String): Minigame {
        if (mapBlackList.contains(mapId)) {
            throw IllegalStateException("Map id of $mapId is already in the map blacklist and cannot be added to the whitelist.")
        }

        mapWhiteList.add(mapId)

        return this
    }

    fun blacklistMap(mapId: String): Minigame {
        if (mapWhiteList.contains(mapId)) {
            throw IllegalStateException("Map id of $mapId is already in the map whitelist and cannot be added to the blacklist.")
        }

        mapBlackList.add(mapId)

        return this
    }

    fun build(): MinigameMetadata {
        return MinigameMetadata(
            description = description,
            isHidden = isHidden,
            mapBlackList = mapBlackList.toSet(),
            mapWhitelist = mapWhiteList.toSet()
        )
    }
}

fun minigame(block: Minigame.() -> Unit): MinigameMetadata {
    return Minigame().apply(block).build()
}