package dev.betrix.superSmashMobsBrawl.models

import dev.betrix.superSmashMobsBrawl.models.brawlData.GameMapDef
import dev.betrix.superSmashMobsBrawl.models.brawlData.HubMapDef
import org.bukkit.World

sealed class BrawlWorld {
    abstract val world: World
}

data class BrawlGameWorld(override val world: World, val data: GameMapDef) : BrawlWorld()

data class BrawlHubWorld(override val world: World, val data: HubMapDef) : BrawlWorld()
