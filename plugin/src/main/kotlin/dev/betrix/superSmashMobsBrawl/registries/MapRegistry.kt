package dev.betrix.superSmashMobsBrawl.registries

import dev.betrix.superSmashMobsBrawl.maps.MapType
import dev.betrix.superSmashMobsBrawl.maps.SsmbMap
import dev.betrix.superSmashMobsBrawl.maps.blueForestHub
import dev.betrix.superSmashMobsBrawl.maps.campsiteMap
import dev.betrix.superSmashMobsBrawl.minigames.definitions.MinigameDefinition

object MapRegistry {
    private val maps = mutableMapOf<String, SsmbMap>()

    fun register(map: SsmbMap) {
        maps[map.id] = map
    }

    fun getDefinition(id: String): SsmbMap? = maps[id]

    fun findClosest(id: String): SsmbMap? {
        if (id.isBlank()) return null

        // First try exact match
        getDefinition(id)?.let {
            return it
        }

        // Then try case-insensitive match
        maps.values
            .find { it.id.equals(id, ignoreCase = true) }
            ?.let {
                return it
            }

        // Finally try partial match (contains)
        return maps.values.find { it.id.contains(id, ignoreCase = true) }
    }

    fun getValidMapsForMinigame(definition: MinigameDefinition): List<SsmbMap> {
        val playerCount = definition.metadata.playersPerTeam * definition.metadata.amountOfTeams

        return maps.filter { (mapId, map) ->
            // Check player capacity and spawn points
            if (map.maxPlayers != null && map.maxPlayers < playerCount) return@filter false
            if (map.spawnPoints.size < playerCount) return@filter false

            // Check blacklist
            if (definition.metadata.mapBlackList.contains(mapId)) return@filter false

            // Check whitelist - if whitelist exists, map must be in it
            if (definition.metadata.mapWhitelist.isNotEmpty()) {
                definition.metadata.mapWhitelist.contains(mapId)
            } else {
                true
            }
        }.values.toList()
    }

    fun getHubMaps(): List<SsmbMap> {
        return maps.values.filter { it.type == MapType.HUB }
    }

    fun getDefaultHub(): SsmbMap? {
        return maps["blue_forest"]
    }

    init {
        register(campsiteMap)
        register(blueForestHub)
    }
}
