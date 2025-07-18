package dev.betrix.superSmashMobsBrawl.maps

val campsiteMap = defineMap {
    name = "Campsite"
    id = "campsite"
    description = "The OG SSMB map"
    voidLevel = 80
    type = MapType.MINIGAME
    maxPlayers = 1 // TODO: `1` is temporary

    addCreator("PLACEHOLDER_UUID")

    spawnPoints {
        at(13.0, 106.0, 33.0)
    }
}