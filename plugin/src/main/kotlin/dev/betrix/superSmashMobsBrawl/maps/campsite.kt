package dev.betrix.superSmashMobsBrawl.maps

val campsiteMap = defineMap {
    name = "Campsite"
    id = "campsite"
    description = "The OG SSMB map"
    voidLevel = 80
    type = MapType.MINIGAME
    maxPlayers = 1 // TODO: `1` is temporary,
    worldBorderSize = 300.0

    addCreator("PLACEHOLDER_UUID")

    spawnPoints {
        at(13.0, 106.0, 33.0)
        at(18.0, 104.0, 62.0)
        at(-31.0, 106.0, 59.0)
        at(-15.0, 107.0, 27.0)
    }
}
