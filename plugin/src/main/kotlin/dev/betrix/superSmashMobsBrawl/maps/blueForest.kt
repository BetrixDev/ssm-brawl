package dev.betrix.superSmashMobsBrawl.maps

val blueForestHub = defineMap {
    name = "Blue Forest Hub"
    id = "blue_forest"
    description = "The main hub world for players to gather and prepare for matches"
    voidLevel = 0
    type = MapType.HUB
    maxPlayers = null // Hubs don't have player limits

    addCreator("PLACEHOLDER_UUID")

    spawnPoints {
        at(0.0, 100.0, 0.0)
    }
}