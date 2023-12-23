package dev.betrix.supersmashmobsbrawl.maps

import dev.betrix.supersmashmobsbrawl.enums.WorldGeneratorType

class EditableMap(private val serverId: String, worldId: String) : SSMBMap(serverId, worldId, WorldGeneratorType.VOID)