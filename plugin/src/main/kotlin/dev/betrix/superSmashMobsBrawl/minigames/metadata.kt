package dev.betrix.superSmashMobsBrawl.minigames

data class MinigameMetadata(
    val description: String = "",
    val isHidden: Boolean = false,
    val playersPerTeam: Int,
    val amountOfTeams: Int,
    val mapWhitelist: Set<String> = setOf(),
    val mapBlackList: Set<String> = setOf(),
    val allowKitSwitching: Boolean = false,
    val stocks: Int = 4,
)

class Minigame {
    var description = ""
    var isHidden = false
    private var mapWhiteList = hashSetOf<String>()
    private var mapBlackList = hashSetOf<String>()
    var playersPerTeam: Int? = null
    var amountOfTeams: Int? = null
    var allowKitSwitching = false
    var stocks = 4

    fun whitelistMap(mapId: String): Minigame {
        require(!mapBlackList.contains(mapId)) {
            "Map id of $mapId is already in the map blacklist and cannot be added to the whitelist."
        }

        mapWhiteList.add(mapId)

        return this
    }

    fun blacklistMap(mapId: String): Minigame {
        require(!mapWhiteList.contains(mapId)) {
            "Map id of $mapId is already in the map whitelist and cannot be added to the blacklist."
        }

        mapBlackList.add(mapId)

        return this
    }

    internal fun build(): MinigameMetadata {
        // Validate required properties with clear error messages
        require(playersPerTeam != null) {
            "playersPerTeam must be set in minigame definition. Add 'playersPerTeam = <value>' to your minigame block."
        }
        require(amountOfTeams != null) {
            "amountOfTeams must be set in minigame definition. Add 'amountOfTeams = <value>' to your minigame block."
        }
        require(playersPerTeam!! > 0) {
            "playersPerTeam must be greater than 0, but was $playersPerTeam"
        }
        require(amountOfTeams!! > 0) {
            "amountOfTeams must be greater than 0, but was $amountOfTeams"
        }

        return MinigameMetadata(
            description = description,
            isHidden = isHidden,
            mapBlackList = mapBlackList.toSet(),
            mapWhitelist = mapWhiteList.toSet(),
            playersPerTeam = playersPerTeam!!,
            amountOfTeams = amountOfTeams!!,
            allowKitSwitching = allowKitSwitching,
            stocks = stocks,
        )
    }
}

fun minigame(block: Minigame.() -> Unit): MinigameMetadata {
    return Minigame().apply(block).build()
}
