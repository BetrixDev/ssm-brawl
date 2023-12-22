package dev.betrix.supersmashmobsbrawl.maps

import dev.betrix.supersmashmobsbrawl.enums.WorldGeneratorType

class GameMap(
    private val serverName: String,
    private val mapId: String
) : SSMBMap(serverName, mapId, WorldGeneratorType.ISLANDS)