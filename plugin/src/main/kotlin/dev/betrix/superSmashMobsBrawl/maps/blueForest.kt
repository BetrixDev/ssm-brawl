package dev.betrix.superSmashMobsBrawl.maps

val blueForestHub = defineMap {
    name = "Blue Forest Hub"
    id = "blue_forest"
    description = "The main hub world for players to gather and prepare for matches"
    voidLevel = 0
    type = MapType.HUB
    maxPlayers = null

    addCreator("PLACEHOLDER_UUID")

    spawnPoints {
        at(-29.5, 58.0, 1.5, 90f, 0f)
    }
}