package dev.betrix.superSmashMobsBrawl.registries

import dev.betrix.superSmashMobsBrawl.maps.SsmbMap
import dev.betrix.superSmashMobsBrawl.maps.MapType
import dev.betrix.superSmashMobsBrawl.maps.campsiteMap
import dev.betrix.superSmashMobsBrawl.maps.blueForestHub

object MapRegistry {
    private val maps = mutableMapOf<String, SsmbMap>()

    fun register(map: SsmbMap) {
        maps[map.id] = map
    }

    fun getDefinition(id: String): SsmbMap? = maps[id]

    fun findClosest(id: String): SsmbMap? {
        if (id.isBlank()) return null

        // First try exact match
        getDefinition(id)?.let { return it }

        // Then try case-insensitive match
        maps.values.find { it.id.equals(id, ignoreCase = true) }?.let { return it }

        // Finally try partial match (contains)
        return maps.values.find { it.id.contains(id, ignoreCase = true) }
    }

    fun getValidMapForPlayerCount(playerCount: Int): SsmbMap {
        TODO("Implement this function")
    }

    fun getHubMaps(): List<SsmbMap> {
        return maps.values.filter { it.type == MapType.HUB }
    }

    fun getDefaultHub(): SsmbMap? {
        return maps.values.find { it.type == MapType.HUB && it.isDefault }
    }

    fun getDefaultHubs(): List<SsmbMap> {
        return maps.values.filter { it.type == MapType.HUB && it.isDefault }
    }

    fun isHubMap(mapId: String): Boolean {
        return maps[mapId]?.type == MapType.HUB
    }

    fun validateHubConfiguration(): Boolean {
        val defaultHubs = getDefaultHubs()
        return defaultHubs.size == 1
    }

    init {
        register(campsiteMap)
        register(blueForestHub)
    }
}
