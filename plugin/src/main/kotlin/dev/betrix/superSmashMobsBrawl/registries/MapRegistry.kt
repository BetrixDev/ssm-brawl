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
            if (map.maxPlayers == null || map.maxPlayers < playerCount || map.spawnPoints.size < playerCount) {
                false
            }

            if (definition.metadata.mapBlackList.contains(mapId)) {
                false
            }

            if (definition.metadata.mapWhitelist.contains(mapId)) {
                true
            }

            definition.metadata.mapWhitelist.isEmpty()
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
